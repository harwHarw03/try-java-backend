package com.airscope.util;

import com.airscope.dynamodb.SensorData;

import java.util.List;

/**
 * AirQualityCalculator - contains all the algorithms for analyzing sensor data.
 *
 * This is a pure utility class (no database access, no Spring beans).
 * It takes data and returns results — easy to understand and test.
 */
public class AirQualityCalculator {

    // --- Air Quality Score ---

    /**
     * Calculate an air quality score from 0 to 100.
     *
     * Formula:
     *   Start at 100 (perfect air)
     *   Subtract penalties for bad readings:
     *     - PM2.5 penalty: each µg/m³ costs 1.5 points
     *     - CO2 penalty:   every 50 ppm costs 1 point
     *     - Humidity penalty: if outside the 40-60% comfort range
     *
     * @return score between 0 and 100
     */
    public static double calculateScore(double pm25, double co2, double humidity) {
        double score = 100.0;

        // PM2.5 penalty — fine particles are dangerous
        score -= (pm25 * 1.5);

        // CO2 penalty — high CO2 causes drowsiness and poor concentration
        score -= (co2 / 50.0);

        // Humidity penalty — outside 40-60% causes discomfort and health issues
        if (humidity < 40.0) {
            score -= (40.0 - humidity) * 0.5; // too dry
        } else if (humidity > 60.0) {
            score -= (humidity - 60.0) * 0.5; // too humid
        }

        // Clamp the score to the 0-100 range
        return Math.max(0, Math.min(100, score));
    }

    /**
     * Convert a numeric score to a human-readable category.
     */
    public static String getCategory(double score) {
        if (score >= 80) return "Good";
        if (score >= 60) return "Moderate";
        if (score >= 40) return "Poor";
        return "Hazardous";
    }

    /**
     * Generate a human-readable explanation for the score.
     */
    public static String getExplanation(double pm25, double co2, double humidity, double score) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Score: %.1f/100. ", score));

        if (pm25 > 35) sb.append("PM2.5 is HIGH (").append(pm25).append(" µg/m³). ");
        if (co2 > 1000) sb.append("CO2 is elevated (").append(co2).append(" ppm). ");
        if (humidity < 40) sb.append("Air is too dry (").append(humidity).append("%). ");
        if (humidity > 60) sb.append("Humidity is too high (").append(humidity).append("%). ");

        if (score >= 80) sb.append("Air quality is good.");

        return sb.toString().trim();
    }

    // --- Moving Average ---

    /**
     * Calculate the moving average of a specific field across N readings.
     *
     * A moving average smooths out short-term fluctuations and shows the overall trend.
     * Example: instead of seeing [850, 900, 870, 920], you'd see the average: 885 ppm.
     *
     * @param readings list of sensor readings (most recent first)
     * @param field    which field to average: "co2", "temperature", "humidity", "pm25"
     * @param n        how many readings to include
     * @return the average value, or 0.0 if no readings
     */
    public static double movingAverage(List<SensorData> readings, String field, int n) {
        if (readings == null || readings.isEmpty()) return 0.0;

        int effectiveSize = Math.min(n, readings.size());
        if (effectiveSize == 0) return 0.0;

        double sum = 0.0;
        int count = 0;

        for (int i = 0; i < effectiveSize; i++) {
            Double value = getFieldValue(readings.get(i), field);
            if (value != null) {
                sum += value;
                count++;
            }
        }

        return count > 0 ? sum / count : 0.0;
    }

    // --- Trend Detection ---

    /**
     * Detect the trend of a metric over recent readings.
     *
     * Simple slope detection:
     *   - Split readings into "first half" and "second half"
     *   - If second half average > first half average by a threshold → INCREASING
     *   - If second half average < first half average by a threshold → DECREASING
     *   - Otherwise → STABLE
     *
     * @param readings list of sensor readings (most recent first)
     * @param field    which field to check
     * @return "INCREASING", "DECREASING", or "STABLE"
     */
    public static String detectTrend(List<SensorData> readings, String field) {
        if (readings == null || readings.size() < 4) {
            return "STABLE";
        }

        int mid = readings.size() / 2;
        if (mid < 2) return "STABLE";

        double recentAvg = movingAverage(readings.subList(0, mid), field, mid);
        double olderAvg = movingAverage(readings.subList(mid, readings.size()), field, readings.size() - mid);

        if (olderAvg == 0) return "STABLE";
        double changePercent = ((recentAvg - olderAvg) / olderAvg) * 100;

        if (changePercent > 5) return "INCREASING";
        if (changePercent < -5) return "DECREASING";
        return "STABLE";
    }

    /**
     * Helper: extract the value of a specific field from a SensorData object.
     */
    private static Double getFieldValue(SensorData reading, String field) {
        return switch (field.toLowerCase()) {
            case "co2"         -> reading.getCo2();
            case "temperature" -> reading.getTemperature();
            case "humidity"    -> reading.getHumidity();
            case "pm25"        -> reading.getPm25();
            default            -> null;
        };
    }
}
