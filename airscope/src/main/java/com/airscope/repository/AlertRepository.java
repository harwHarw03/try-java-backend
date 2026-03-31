package com.airscope.repository;

import com.airscope.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AlertRepository - handles database operations for the Alert entity.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Find all alerts configured for a specific device.
     * → SELECT * FROM alerts WHERE device_id = ?
     */
    List<Alert> findByDeviceId(Long deviceId);
}
