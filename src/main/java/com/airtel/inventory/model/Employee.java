package com.airtel.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    
    @Id
    @Column(length = 100)
    private String id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 100)
    private String department;
    
    @Column(length = 100)
    private String designation;
    
    @Column(unique = true, length = 200)
    private String email;
    
    @Column(length = 50)
    private String phone;
    
    @Column(name = "joining_date")
    private LocalDate joiningDate;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}