package com.airtel.inventory.repository;

import com.airtel.inventory.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    
    List<Issue> findByDeviceId(Long deviceId);
    
    List<Issue> findBySeverity(String severity);
    
    @Query("SELECT COUNT(i) FROM Issue i WHERE i.deviceId = :deviceId AND i.reportedDate >= :startDate")
    long countIssuesSince(@Param("deviceId") Long deviceId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT i FROM Issue i WHERE i.resolvedDate IS NULL AND i.severity IN ('HIGH', 'CRITICAL')")
    List<Issue> findUnresolvedCriticalIssues();
}