package com.airtel.inventory.service;

import com.airtel.inventory.dto.DeviceDTO;
import com.airtel.inventory.model.Device;
import com.airtel.inventory.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeviceService {
    
    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);
    
    private final DeviceRepository deviceRepository;
    private final QRCodeService qrCodeService;
    private final EmailService emailService;
    private final WebSocketService webSocketService;
    
    // Updated constructor
    public DeviceService(DeviceRepository deviceRepository, 
                         QRCodeService qrCodeService,
                         EmailService emailService,
                         WebSocketService webSocketService) {
        this.deviceRepository = deviceRepository;
        this.qrCodeService = qrCodeService;
        this.emailService = emailService;
        this.webSocketService = webSocketService;
    }
    
    public Device registerDevice(DeviceDTO deviceDTO) {
        log.info("Registering new device: {}", deviceDTO.getAssetTag());
        
        Device device = new Device();
        device.setAssetTag(deviceDTO.getAssetTag());
        device.setSerialNumber(deviceDTO.getSerialNumber());
        device.setDeviceType(deviceDTO.getDeviceType());
        device.setBrand(deviceDTO.getBrand());
        device.setModel(deviceDTO.getModel());
        device.setSpecifications(deviceDTO.getSpecifications());
        device.setPurchaseDate(deviceDTO.getPurchaseDate());
        device.setWarrantyEnd(deviceDTO.getWarrantyEnd());
        device.setCost(deviceDTO.getCost());
        device.setCurrentStatus("AVAILABLE");
        device.setHealthScore(100);
        device.setConditionStatus("GOOD");
        
        Device savedDevice = deviceRepository.save(device);
        
        // Generate QR Code (with error handling)
        try {
            String qrPath = qrCodeService.generateQRCode(savedDevice);
            savedDevice.setQrCodePath(qrPath);
        } catch (Exception e) {
            log.warn("QR Code generation failed: {}", e.getMessage());
        }
        
        // Generate Barcode (with error handling)
        try {
            String barcodePath = qrCodeService.generateBarcode(savedDevice);
            savedDevice.setBarcodePath(barcodePath);
        } catch (Exception e) {
            log.warn("Barcode generation failed: {}", e.getMessage());
        }
        
        Device finalDevice = deviceRepository.save(savedDevice);
        
        // Send email notification to admin
        emailService.sendDeviceRegistrationNotification(finalDevice);
        
        // Send WebSocket real-time updates
        webSocketService.sendDeviceUpdate(String.valueOf(finalDevice.getId()), "REGISTERED", finalDevice);
        webSocketService.sendStatisticsUpdate(getStatistics());
        
        return finalDevice;
    }
    
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }
    
    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
    }
    
    public Device getDeviceByAssetTag(String assetTag) {
        return deviceRepository.findByAssetTag(assetTag)
            .orElseThrow(() -> new RuntimeException("Device not found with tag: " + assetTag));
    }
    
    public Device updateDeviceStatus(Long id, String status) {
        Device device = getDeviceById(id);
        device.setCurrentStatus(status);
        device.setUpdatedAt(LocalDateTime.now());
        Device updated = deviceRepository.save(device);
        
        // Send WebSocket update
        webSocketService.sendDeviceUpdate(String.valueOf(id), "STATUS_UPDATED", updated);
        webSocketService.sendStatisticsUpdate(getStatistics());
        
        return updated;
    }
    
    public Device updateDeviceCondition(Long id, String condition) {
        Device device = getDeviceById(id);
        device.setConditionStatus(condition);
        device.setUpdatedAt(LocalDateTime.now());
        
        int healthAdjustment;
        switch (condition) {
            case "EXCELLENT":
                healthAdjustment = 0;
                break;
            case "GOOD":
                healthAdjustment = -5;
                break;
            case "FAIR":
                healthAdjustment = -15;
                break;
            case "POOR":
                healthAdjustment = -30;
                break;
            case "DAMAGED":
                healthAdjustment = -50;
                break;
            default:
                healthAdjustment = 0;
        }
        
        int newHealthScore = Math.max(0, device.getHealthScore() + healthAdjustment);
        device.setHealthScore(newHealthScore);
        
        Device updated = deviceRepository.save(device);
        
        // Send WebSocket update
        webSocketService.sendDeviceUpdate(String.valueOf(id), "CONDITION_UPDATED", updated);
        webSocketService.sendStatisticsUpdate(getStatistics());
        
        return updated;
    }
    
    public List<Device> getAvailableDevices() {
        return deviceRepository.findByCurrentStatus("AVAILABLE");
    }
    
    public List<Device> getHighRiskDevices() {
        return deviceRepository.findByHealthScoreLessThan(40);
    }
    
    public List<Device> getDevicesNeedingMaintenance() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        List<Device> allDevices = deviceRepository.findAll();
        return allDevices.stream()
            .filter(d -> d.getLastMaintenanceDate() == null || 
                        d.getLastMaintenanceDate().isBefore(sixMonthsAgo))
            .collect(Collectors.toList());
    }
    
    public List<Device> searchDevices(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        List<Device> allDevices = deviceRepository.findAll();
        return allDevices.stream()
            .filter(d -> d.getAssetTag().toLowerCase().contains(lowerKeyword) ||
                        d.getSerialNumber().toLowerCase().contains(lowerKeyword) ||
                        (d.getBrand() != null && d.getBrand().toLowerCase().contains(lowerKeyword)) ||
                        (d.getModel() != null && d.getModel().toLowerCase().contains(lowerKeyword)))
            .collect(Collectors.toList());
    }
    
    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
        log.info("Deleted device with id: {}", id);
        
        // Send WebSocket update
        webSocketService.sendDeviceUpdate(String.valueOf(id), "DELETED", null);
        webSocketService.sendStatisticsUpdate(getStatistics());
    }
    
    public DeviceStatistics getStatistics() {
        DeviceStatistics stats = new DeviceStatistics();
        stats.totalDevices = deviceRepository.count();
        stats.availableDevices = deviceRepository.countByStatus("AVAILABLE");
        stats.assignedDevices = deviceRepository.countByStatus("ASSIGNED");
        stats.underRepair = deviceRepository.countByStatus("UNDER_REPAIR");
        stats.averageHealthScore = deviceRepository.getAverageHealthScore();
        stats.highRiskDevices = deviceRepository.findByHealthScoreLessThan(40).size();
        return stats;
    }
    
    public static class DeviceStatistics {
        private long totalDevices;
        private long availableDevices;
        private long assignedDevices;
        private long underRepair;
        private Double averageHealthScore;
        private int highRiskDevices;
        
        public long getTotalDevices() { return totalDevices; }
        public long getAvailableDevices() { return availableDevices; }
        public long getAssignedDevices() { return assignedDevices; }
        public long getUnderRepair() { return underRepair; }
        public Double getAverageHealthScore() { return averageHealthScore; }
        public int getHighRiskDevices() { return highRiskDevices; }
        
        public void setTotalDevices(long totalDevices) { this.totalDevices = totalDevices; }
        public void setAvailableDevices(long availableDevices) { this.availableDevices = availableDevices; }
        public void setAssignedDevices(long assignedDevices) { this.assignedDevices = assignedDevices; }
        public void setUnderRepair(long underRepair) { this.underRepair = underRepair; }
        public void setAverageHealthScore(Double averageHealthScore) { this.averageHealthScore = averageHealthScore; }
        public void setHighRiskDevices(int highRiskDevices) { this.highRiskDevices = highRiskDevices; }
    }
}