package com.airscope.repository;

import com.airscope.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - handles database operations for the User entity.
 *
 * By extending JpaRepository, Spring Data automatically gives us:
 *   - save()        → INSERT or UPDATE
 *   - findById()    → SELECT by primary key
 *   - findAll()     → SELECT all
 *   - delete()      → DELETE
 *   - count()       → COUNT
 *
 * We only need to define custom queries (like findByEmail).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Spring Data auto-generates SQL from the method name:
     * "findByEmail" → SELECT * FROM users WHERE email = ?
     *
     * Optional<User> means it might return null (user not found),
     * so we handle it safely without null checks.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if an email already exists (used during registration).
     * "existsBy" → SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
     */
    boolean existsByEmail(String email);
}
