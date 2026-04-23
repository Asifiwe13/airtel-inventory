package com.airtel.inventory.repository;

import com.airtel.inventory.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    
    Optional<Device> findByAssetTag(String assetTag);
    
    Optional<Device> findBySerialNumber(String serialNumber);
    
    List<Device> findByDeviceType(String deviceType);
    
    List<Device> findByCurrentStatus(String status);
    
    List<Device> findByHealthScoreLessThan(int healthScore);
    
    @Query("SELECT d FROM Device d WHERE d.currentStatus = 'AVAILABLE' AND d.deviceType = :type")
    List<Device> findAvailableDevicesByType(@Param("type") String type);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.currentStatus = :status")
    long countByStatus(@Param("status") String status);
    
    @Query("SELECT AVG(d.healthScore) FROM Device d")
    Double getAverageHealthScore();
}