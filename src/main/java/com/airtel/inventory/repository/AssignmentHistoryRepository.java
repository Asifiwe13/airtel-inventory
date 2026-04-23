package com.airtel.inventory.repository;

import com.airtel.inventory.model.AssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistory, Long> {
    
    List<AssignmentHistory> findByDeviceId(Long deviceId);
    
    List<AssignmentHistory> findByEmployeeId(String employeeId);
    
    @Query("SELECT a FROM AssignmentHistory a WHERE a.deviceId = :deviceId AND a.actualReturnDate IS NULL")
    Optional<AssignmentHistory> findCurrentAssignmentByDevice(@Param("deviceId") Long deviceId);
    
    @Query("SELECT a FROM AssignmentHistory a WHERE a.employeeId = :employeeId AND a.actualReturnDate IS NULL")
    List<AssignmentHistory> findCurrentAssignmentsByEmployee(@Param("employeeId") String employeeId);
    
    @Query("SELECT COUNT(a) FROM AssignmentHistory a WHERE a.deviceId = :deviceId")
    long countAssignmentsByDevice(@Param("deviceId") Long deviceId);
    
    @Query("SELECT a FROM AssignmentHistory a WHERE a.isAnomaly = true")
    List<AssignmentHistory> findAnomalousAssignments();
    
    @Query("SELECT a FROM AssignmentHistory a WHERE a.actualReturnDate BETWEEN :startDate AND :endDate")
    List<AssignmentHistory> findReturnsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
}