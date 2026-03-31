package com.airscope;

import com.airscope.dynamodb.SensorData;
import com.airscope.util.AirQualityCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AirQualityCalculator.
 *
 * These tests don't need a database or server — they test pure logic.
 * Run with: mvn test
 */
class AirQualityCalculatorTest {

    // --- Score tests ---

    @Test
    void perfectAirShouldScoreHigh() {
        // Good air: low PM2.5, low CO2, ideal humidity
        double score = AirQualityCalculator.calculateScore(0, 400, 50);
        // score = 100 - 0 - 8 - 0 = 92
        assertEquals(92.0, score, 0.01);
    }

    @Test
    void highPm25ShouldLowerScore() {
        double score = AirQualityCalculator.calculateScore(30, 400, 50);
        // score = 100 - 45 - 8 - 0 = 47
        assertEquals(47.0, score, 0.01);
    }

    @Test
    void scoreShouldNeverGoBelowZero() {
        // Extreme values — score should be clamped at 0
        double score = AirQualityCalculator.calculateScore(100, 5000, 90);
        assertEquals(0.0, score, 0.01);
    }

    @Test
    void scoreShouldNeverExceed100() {
        // Perfect conditions
        double score = AirQualityCalculator.calculateScore(0, 0, 50);
        assertEquals(100.0, score, 0.01);
    }

    // --- Category tests ---

    @Test
    void scoreof85IsGood() {
        assertEquals("Good", AirQualityCalculator.getCategory(85));
    }

    @Test
    void scoreOf65IsModerate() {
        assertEquals("Moderate", AirQualityCalculator.getCategory(65));
    }

    @Test
    void scoreOf45IsPoor() {
        assertEquals("Poor", AirQualityCalculator.getCategory(45));
    }

    @Test
    void scoreOf20IsHazardous() {
        assertEquals("Hazardous", AirQualityCalculator.getCategory(20));
    }

    // --- Moving average tests ---

    @Test
    void movingAverageCalculatesCorrectly() {
        List<SensorData> readings = List.of(
                SensorData.builder().co2(900.0).build(),
                SensorData.builder().co2(1000.0).build(),
                SensorData.builder().co2(800.0).build()
        );
        double avg = AirQualityCalculator.movingAverage(readings, "co2", 3);
        assertEquals(900.0, avg, 0.01);
    }

    @Test
    void movingAverageReturnsZeroForEmptyList() {
        double avg = AirQualityCalculator.movingAverage(List.of(), "co2", 5);
        assertEquals(0.0, avg, 0.01);
    }

    // --- Trend detection tests ---

    @Test
    void increasingCo2DetectedAsTrendUp() {
        // Newer readings (index 0) have higher CO2 → INCREASING trend
        List<SensorData> readings = List.of(
                SensorData.builder().co2(1200.0).build(), // newest
                SensorData.builder().co2(1100.0).build(),
                SensorData.builder().co2(800.0).build(),
                SensorData.builder().co2(700.0).build()  // oldest
        );
        String trend = AirQualityCalculator.detectTrend(readings, "co2");
        assertEquals("INCREASING", trend);
    }

    @Test
    void stableCo2IsDetectedAsStable() {
        List<SensorData> readings = List.of(
                SensorData.builder().co2(850.0).build(),
                SensorData.builder().co2(860.0).build(),
                SensorData.builder().co2(840.0).build(),
                SensorData.builder().co2(855.0).build()
        );
        String trend = AirQualityCalculator.detectTrend(readings, "co2");
        assertEquals("STABLE", trend);
    }

    @Test
    void tooFewReadingsReturnsStable() {
        List<SensorData> readings = List.of(
                SensorData.builder().co2(900.0).build()
        );
        String trend = AirQualityCalculator.detectTrend(readings, "co2");
        assertEquals("STABLE", trend);
    }
}
