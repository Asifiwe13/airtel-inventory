package com.airtel.inventory.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_history")
public class AssignmentHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id", nullable = false)
    private Long deviceId;
    
    @Column(name = "employee_id", nullable = false, length = 100)
    private String employeeId;
    
    @Column(name = "assigned_date", nullable = false)
    private LocalDateTime assignedDate;
    
    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;
    
    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;
    
    @Column(name = "condition_at_assignment", length = 50)
    private String conditionAtAssignment;
    
    @Column(name = "condition_at_return", length = 50)
    private String conditionAtReturn;
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "audit_hash", length = 256)
    private String auditHash;
    
    @Column(name = "is_anomaly")
    private Boolean isAnomaly = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Default constructor
    public AssignmentHistory() {}
    
    // Getters
    public Long getId() { return id; }
    public Long getDeviceId() { return deviceId; }
    public String getEmployeeId() { return employeeId; }
    public LocalDateTime getAssignedDate() { return assignedDate; }
    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
    public LocalDateTime getActualReturnDate() { return actualReturnDate; }
    public String getConditionAtAssignment() { return conditionAtAssignment; }
    public String getConditionAtReturn() { return conditionAtReturn; }
    public String getRemarks() { return remarks; }
    public String getAuditHash() { return auditHash; }
    public Boolean getIsAnomaly() { return isAnomaly; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }
    public void setActualReturnDate(LocalDateTime actualReturnDate) { this.actualReturnDate = actualReturnDate; }
    public void setConditionAtAssignment(String conditionAtAssignment) { this.conditionAtAssignment = conditionAtAssignment; }
    public void setConditionAtReturn(String conditionAtReturn) { this.conditionAtReturn = conditionAtReturn; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public void setAuditHash(String auditHash) { this.auditHash = auditHash; }
    public void setIsAnomaly(Boolean isAnomaly) { this.isAnomaly = isAnomaly; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (assignedDate == null) {
            assignedDate = LocalDateTime.now();
        }
    }
}