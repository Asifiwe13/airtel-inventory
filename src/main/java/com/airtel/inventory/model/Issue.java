package com.airtel.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Issue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id", nullable = false)
    private Long deviceId;
    
    @Column(name = "reported_by_employee_id", length = 100)
    private String reportedByEmployeeId;
    
    @Column(name = "issue_category", length = 50)
    private String issueCategory;
    
    @Column(length = 20)
    private String severity;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "reported_date", nullable = false)
    private LocalDateTime reportedDate;
    
    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    @Column(name = "repair_cost")
    private BigDecimal repairCost;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (reportedDate == null) {
            reportedDate = LocalDateTime.now();
        }
    }
}