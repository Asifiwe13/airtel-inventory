package com.airtel.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "device")
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "asset_tag", unique = true, nullable = false, length = 50)
    private String assetTag;
    
    @Column(name = "serial_number", unique = true, nullable = false, length = 100)
    private String serialNumber;
    
    @Column(name = "device_type", nullable = false, length = 50)
    private String deviceType;
    
    @Column(length = 100)
    private String brand;
    
    @Column(length = 100)
    private String model;
    
    @Column(columnDefinition = "TEXT")
    private String specifications;
    
    @Column(name = "condition_status", length = 50)
    private String conditionStatus = "GOOD";
    
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    
    @Column(name = "warranty_end")
    private LocalDate warrantyEnd;
    
    private BigDecimal cost;
    
    @Column(name = "current_owner_id", length = 100)
    private String currentOwnerId;
    
    @Column(name = "current_status", length = 50)
    private String currentStatus = "AVAILABLE";
    
    @Column(name = "qr_code_path", length = 500)
    private String qrCodePath;
    
    @Column(name = "barcode_path", length = 500)  // ✅ ADD THIS FIELD
    private String barcodePath;
    
    @Column(name = "health_score")
    private Integer healthScore = 100;
    
    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Device() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }
    
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    
    public String getConditionStatus() { return conditionStatus; }
    public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }
    
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public LocalDate getWarrantyEnd() { return warrantyEnd; }
    public void setWarrantyEnd(LocalDate warrantyEnd) { this.warrantyEnd = warrantyEnd; }
    
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    
    public String getCurrentOwnerId() { return currentOwnerId; }
    public void setCurrentOwnerId(String currentOwnerId) { this.currentOwnerId = currentOwnerId; }
    
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    
    public String getQrCodePath() { return qrCodePath; }
    public void setQrCodePath(String qrCodePath) { this.qrCodePath = qrCodePath; }
    
    public String getBarcodePath() { return barcodePath; }  // ✅ ADD
    public void setBarcodePath(String barcodePath) { this.barcodePath = barcodePath; }  // ✅ ADD
    
    public Integer getHealthScore() { return healthScore; }
    public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
    
    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public int getAgeInMonths() {
        if (purchaseDate == null) return 0;
        return (int) java.time.temporal.ChronoUnit.MONTHS.between(purchaseDate, LocalDate.now());
    }
}