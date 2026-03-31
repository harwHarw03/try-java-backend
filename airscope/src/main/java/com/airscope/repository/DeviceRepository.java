package com.airscope.repository;

import com.airscope.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DeviceRepository - handles database operations for the Device entity.
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    /**
     * Find all devices belonging to a specific user.
     * → SELECT * FROM devices WHERE user_id = ?
     */
    List<Device> findByUserId(Long userId);

    /**
     * Find a specific device that belongs to a specific user.
     * This is used for security — users should only access their own devices.
     * → SELECT * FROM devices WHERE id = ? AND user_id = ?
     */
    Optional<Device> findByIdAndUserId(Long id, Long userId);
}
