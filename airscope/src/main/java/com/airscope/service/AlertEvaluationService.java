package com.airscope.service;

import com.airscope.dynamodb.SensorData;
import com.airscope.dynamodb.SensorDataRepository;
import com.airscope.model.Alert;
import com.airscope.repository.AlertRepository;
import com.airscope.util.DeviceOwnershipValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertEvaluationService {

    private final AlertRepository alertRepository;
    private final SensorDataRepository sensorDataRepository;
    private final DeviceOwnershipValidator ownershipValidator;

    public record TriggeredAlert(Long alertId, String deviceId, String type, 
                                  double threshold, double currentValue) {}

    /**
     * Evaluate all active alerts for a device against the latest sensor reading.
     */
    public List<TriggeredAlert> evaluateDeviceAlerts(String deviceId, String userEmail) {
        ownershipValidator.validateOwnership(deviceId, userEmail);

        List<Alert> alerts = alertRepository.findAll().stream()
                .filter(a -> a.getDevice().getId().toString().equals(deviceId))
                .toList();

        List<SensorData> readings = sensorDataRepository.findByDeviceId(deviceId, 1);
        if (readings.isEmpty()) {
            return List.of();
        }

        SensorData latest = readings.get(0);
        List<TriggeredAlert> triggered = new ArrayList<>();

        for (Alert alert : alerts) {
            if (isThresholdExceeded(alert, latest)) {
                triggered.add(new TriggeredAlert(
                        alert.getId(),
                        deviceId,
                        alert.getType(),
                        alert.getThreshold(),
                        getMetricValue(alert.getType(), latest)
                ));
            }
        }

        return triggered;
    }

    /**
     * Scheduled job to check all devices for threshold violations.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000)
    public void evaluateAllAlerts() {
        log.info("Running scheduled alert evaluation...");
        
        List<Alert> allAlerts = alertRepository.findAll();
        
        for (Alert alert : allAlerts) {
            String deviceId = alert.getDevice().getId().toString();
            List<SensorData> readings = sensorDataRepository.findByDeviceId(deviceId, 1);
            
            if (!readings.isEmpty() && isThresholdExceeded(alert, readings.get(0))) {
                log.warn("ALERT TRIGGERED - Device: {}, Type: {}, Threshold: {}, Current: {}",
                        deviceId, alert.getType(), alert.getThreshold(),
                        getMetricValue(alert.getType(), readings.get(0)));
            }
        }
    }

    private boolean isThresholdExceeded(Alert alert, SensorData reading) {
        double currentValue = getMetricValue(alert.getType(), reading);
        return currentValue > alert.getThreshold();
    }

    private double getMetricValue(String type, SensorData reading) {
        return switch (type.toUpperCase()) {
            case "CO2" -> reading.getCo2() != null ? reading.getCo2() : 0;
            case "TEMP" -> reading.getTemperature() != null ? reading.getTemperature() : 0;
            case "PM25" -> reading.getPm25() != null ? reading.getPm25() : 0;
            case "HUMIDITY" -> reading.getHumidity() != null ? reading.getHumidity() : 0;
            default -> 0;
        };
    }
}
