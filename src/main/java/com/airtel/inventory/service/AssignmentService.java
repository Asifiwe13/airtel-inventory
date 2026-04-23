package com.airtel.inventory.service;

import com.airtel.inventory.model.AssignmentHistory;
import com.airtel.inventory.model.Device;
import com.airtel.inventory.model.Employee;
import com.airtel.inventory.repository.AssignmentHistoryRepository;
import com.airtel.inventory.repository.DeviceRepository;
import com.airtel.inventory.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssignmentService {
    
    private static final Logger log = LoggerFactory.getLogger(AssignmentService.class);
    
    private final AssignmentHistoryRepository assignmentRepository;
    private final DeviceRepository deviceRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final WebSocketService webSocketService;
    private final DeviceService deviceService;  // Added for statistics
    
    public AssignmentService(AssignmentHistoryRepository assignmentRepository,
                             DeviceRepository deviceRepository,
                             EmployeeRepository employeeRepository,
                             EmailService emailService,
                             WebSocketService webSocketService,
                             DeviceService deviceService) {
        this.assignmentRepository = assignmentRepository;
        this.deviceRepository = deviceRepository;
        this.employeeRepository = employeeRepository;
        this.emailService = emailService;
        this.webSocketService = webSocketService;
        this.deviceService = deviceService;
    }
    
    @Transactional
    public AssignmentHistory assignDevice(Long deviceId, String employeeId, 
                                          LocalDate expectedReturnDate, 
                                          String conditionAtAssignment, 
                                          String remarks) {
        
        log.info("📋 Starting device assignment - Device ID: {}, Employee ID: {}", deviceId, employeeId);
        
        // Validate device
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new RuntimeException("Device not found"));
        
        if (!"AVAILABLE".equals(device.getCurrentStatus())) {
            throw new RuntimeException("Device is not available for assignment. Current status: " + device.getCurrentStatus());
        }
        
        // Validate employee
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        log.info("📧 Employee email for notification: {}", employee.getEmail());
        
        // Check employee assignment limit
        long activeAssignments = assignmentRepository.findCurrentAssignmentsByEmployee(employeeId).size();
        if (activeAssignments >= 5) {
            throw new RuntimeException("Employee already has " + activeAssignments + " devices. Maximum limit is 5.");
        }
        
        // Create assignment
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setDeviceId(deviceId);
        assignment.setEmployeeId(employeeId);
        assignment.setAssignedDate(LocalDateTime.now());
        assignment.setExpectedReturnDate(expectedReturnDate);
        assignment.setConditionAtAssignment(conditionAtAssignment != null ? conditionAtAssignment : device.getConditionStatus());
        assignment.setRemarks(remarks);
        assignment.setAuditHash(generateAuditHash(device, employee));
        assignment.setIsAnomaly(false);
        
        // Update device status
        device.setCurrentStatus("ASSIGNED");
        device.setCurrentOwnerId(employeeId);
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(device);
        
        AssignmentHistory saved = assignmentRepository.save(assignment);
        log.info("✅ Device {} assigned to employee {}", device.getAssetTag(), employee.getName());
        
        // ✅ SEND EMAIL NOTIFICATION
        try {
            log.info("📧 Attempting to send assignment email to: {}", employee.getEmail());
            emailService.sendDeviceAssignmentNotification(device, employee, saved);
            log.info("✅ Assignment email sent successfully to: {}", employee.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send assignment email: {}", e.getMessage());
        }
        
        // ✅ SEND WEBSOCKET REAL-TIME UPDATES
        try {
            webSocketService.sendDeviceUpdate(String.valueOf(deviceId), "ASSIGNED", saved);
            webSocketService.sendAssignmentUpdate(employeeId, "CREATED", saved);
            webSocketService.sendStatisticsUpdate(deviceService.getStatistics());
            log.info("✅ WebSocket updates sent for assignment");
        } catch (Exception e) {
            log.error("❌ Failed to send WebSocket update: {}", e.getMessage());
        }
        
        return saved;
    }
    
    @Transactional
    public AssignmentHistory returnDevice(Long assignmentId, String conditionAtReturn, String remarks) {
        log.info("📋 Processing device return - Assignment ID: {}", assignmentId);
        
        AssignmentHistory assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment record not found"));
        
        if (assignment.getActualReturnDate() != null) {
            throw new RuntimeException("Device already returned on " + assignment.getActualReturnDate());
        }
        
        Device device = deviceRepository.findById(assignment.getDeviceId())
            .orElseThrow(() -> new RuntimeException("Device not found"));
        
        Employee employee = employeeRepository.findById(assignment.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        assignment.setActualReturnDate(LocalDateTime.now());
        assignment.setConditionAtReturn(conditionAtReturn);
        if (remarks != null && !remarks.isEmpty()) {
            assignment.setRemarks(assignment.getRemarks() + " | Return remarks: " + remarks);
        }
        
        // Check if returned late
        if (assignment.getExpectedReturnDate() != null && 
            LocalDate.now().isAfter(assignment.getExpectedReturnDate())) {
            long daysLate = ChronoUnit.DAYS.between(assignment.getExpectedReturnDate(), LocalDate.now());
            assignment.setIsAnomaly(true);
            log.warn("⚠️ Device {} returned {} days late", device.getAssetTag(), daysLate);
        }
        
        // Update device status
        device.setCurrentStatus("AVAILABLE");
        device.setCurrentOwnerId(null);
        device.setConditionStatus(conditionAtReturn);
        
        int healthAdjustment = calculateHealthAdjustment(conditionAtReturn);
        int newHealthScore = Math.max(0, device.getHealthScore() + healthAdjustment);
        device.setHealthScore(newHealthScore);
        device.setLastMaintenanceDate(LocalDate.now());
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(device);
        
        AssignmentHistory saved = assignmentRepository.save(assignment);
        log.info("✅ Device {} returned by {}", device.getAssetTag(), employee.getName());
        
        // ✅ SEND RETURN CONFIRMATION EMAIL
        try {
            emailService.sendDeviceReturnNotification(device, employee, saved);
            log.info("✅ Return confirmation email sent to: {}", employee.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send return email: {}", e.getMessage());
        }
        
        // ✅ SEND WEBSOCKET REAL-TIME UPDATES
        try {
            webSocketService.sendDeviceUpdate(String.valueOf(assignment.getDeviceId()), "RETURNED", saved);
            webSocketService.sendStatisticsUpdate(deviceService.getStatistics());
            log.info("✅ WebSocket updates sent for return");
        } catch (Exception e) {
            log.error("❌ Failed to send WebSocket update: {}", e.getMessage());
        }
        
        return saved;
    }
    
    public List<AssignmentHistory> getDeviceAssignmentHistory(Long deviceId) {
        return assignmentRepository.findByDeviceId(deviceId);
    }
    
    public List<AssignmentHistory> getEmployeeAssignmentHistory(String employeeId) {
        return assignmentRepository.findByEmployeeId(employeeId);
    }
    
    public List<AssignmentHistory> getCurrentAssignments() {
        return assignmentRepository.findAll().stream()
            .filter(a -> a.getActualReturnDate() == null)
            .collect(Collectors.toList());
    }
    
    public List<AssignmentHistory> getOverdueAssignments() {
        return assignmentRepository.findAll().stream()
            .filter(a -> a.getActualReturnDate() == null && 
                        a.getExpectedReturnDate() != null && 
                        LocalDate.now().isAfter(a.getExpectedReturnDate()))
            .collect(Collectors.toList());
    }
    
    public Map<String, Object> getAssignmentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalAssignments = assignmentRepository.count();
        long activeAssignments = getCurrentAssignments().size();
        long overdueAssignments = getOverdueAssignments().size();
        long completedAssignments = totalAssignments - activeAssignments;
        
        double avgDuration = assignmentRepository.findAll().stream()
            .filter(a -> a.getActualReturnDate() != null)
            .mapToLong(a -> ChronoUnit.DAYS.between(a.getAssignedDate(), a.getActualReturnDate()))
            .average()
            .orElse(0);
        
        stats.put("totalAssignments", totalAssignments);
        stats.put("activeAssignments", activeAssignments);
        stats.put("completedAssignments", completedAssignments);
        stats.put("overdueAssignments", overdueAssignments);
        stats.put("averageAssignmentDays", Math.round(avgDuration));
        
        return stats;
    }
    
    private String generateAuditHash(Device device, Employee employee) {
        String data = device.getId() + "|" + device.getAssetTag() + "|" + 
                      employee.getId() + "|" + employee.getName() + "|" + 
                      LocalDateTime.now() + "|" + UUID.randomUUID();
        return Integer.toHexString(data.hashCode());
    }
    
    private int calculateHealthAdjustment(String condition) {
        if (condition == null) return 0;
        switch (condition.toUpperCase()) {
            case "EXCELLENT": return 5;
            case "GOOD": return 0;
            case "FAIR": return -10;
            case "POOR": return -25;
            case "DAMAGED": return -40;
            default: return 0;
        }
    }
}