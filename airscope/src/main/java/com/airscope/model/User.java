package com.airscope.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * User entity - stored in PostgreSQL.
 *
 * Represents a registered user in the system.
 * Passwords are always stored encrypted (BCrypt).
 */
@Data                   // Lombok: generates getters, setters, toString, equals, hashCode
@Builder                // Lombok: lets us use User.builder().email(...).build()
@NoArgsConstructor      // Lombok: generates a no-args constructor (required by JPA)
@AllArgsConstructor     // Lombok: generates a constructor with all fields
@Entity
@Table(name = "users")  // "user" is a reserved word in PostgreSQL, so we use "users"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment ID
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // stored as BCrypt hash, never plain text!

    // Role can be "ROLE_USER" or "ROLE_ADMIN"
    @Column(nullable = false)
    private String role;
}
