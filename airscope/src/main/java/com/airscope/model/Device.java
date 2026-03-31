package com.airscope.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Device entity - stored in PostgreSQL.
 *
 * Represents an IoT sensor device registered by a user.
 * Each device belongs to one user (many-to-one relationship).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g., "Living Room Sensor", "Office Air Monitor"

    // Many devices can belong to one user
    // @ManyToOne: links this device to a User object
    // @JoinColumn: specifies the foreign key column in the "devices" table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
