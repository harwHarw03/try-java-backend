package com.airscope.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Alert entity - stored in PostgreSQL.
 *
 * Represents a threshold alert configured for a device.
 * When sensor data exceeds the threshold, an alert is triggered.
 *
 * Example: "Alert me when CO2 > 1000 ppm on device #3"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Type of measurement to monitor: CO2, TEMP, PM25, HUMIDITY
    @Column(nullable = false)
    private String type;

    // The value that triggers the alert (e.g., 1000 for CO2 ppm)
    @Column(nullable = false)
    private Double threshold;

    // Which device this alert is attached to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;
}
