package com.airtel.inventory.service;

import com.airtel.inventory.model.Device;
import com.airtel.inventory.model.AssignmentHistory;
import com.airtel.inventory.repository.DeviceRepository;
import com.airtel.inventory.repository.AssignmentHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIService {
    
    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    
    private final DeviceRepository deviceRepository;
    private final AssignmentHistoryRepository assignmentRepository;
    
    public AIService(DeviceRepository deviceRepository, 
                     AssignmentHistoryRepository assignmentRepository) {
        this.deviceRepository = deviceRepository;
        this.assignmentRepository = assignmentRepository;
    }
    
    // ===== 1. PREDICTIVE MAINTENANCE =====
    public Map<String, Object> predictDeviceFailure(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new RuntimeException("Device not found"));
        
        Map<String, Object> prediction = new HashMap<>();
        
        double ageFactor = calculateAgeFactor(device);
        double issueFactor = calculateIssueFactor(device);
        double maintenanceFactor = calculateMaintenanceFactor(device);
        double usageFactor = calculateUsageFactor(device);
        
        double failureProbability = (ageFactor * 0.3) + (issueFactor * 0.35) + 
                                     (maintenanceFactor * 0.2) + (usageFactor * 0.15);
        
        // Convert to percentage (integer)
        int failurePercent = (int) Math.round(failureProbability * 100);
        
        prediction.put("deviceId", deviceId);
        prediction.put("assetTag", device.getAssetTag());
        prediction.put("failureProbability", failurePercent);  // ✅ Now Integer, not Double
        prediction.put("riskLevel", getRiskLevel(failureProbability));
        prediction.put("predictedFailureDate", predictFailureDate(device, failureProbability));
        prediction.put("recommendedAction", getRecommendedAction(failureProbability));
        
        log.info("AI Prediction for device {}: {}% failure risk", device.getAssetTag(), failurePercent);
        
        return prediction;
    }
    
    public List<Map<String, Object>> getHighRiskDevices() {
        List<Device> devices = deviceRepository.findAll();
        List<Map<String, Object>> highRiskDevices = new ArrayList<>();
        
        for (Device device : devices) {
            Map<String, Object> prediction = predictDeviceFailure(device.getId());
            // ✅ FIXED: Get as Integer, not Double
            int risk = (int) prediction.get("failureProbability");
            if (risk > 60) {
                highRiskDevices.add(prediction);
            }
        }
        
        return highRiskDevices.stream()
            .sorted((a, b) -> {
                int riskA = (int) a.get("failureProbability");
                int riskB = (int) b.get("failureProbability");
                return Integer.compare(riskB, riskA);
            })
            .limit(10)
            .collect(Collectors.toList());
    }
    
    private double calculateAgeFactor(Device device) {
        int ageMonths = device.getAgeInMonths();
        if (ageMonths <= 12) return 0.1;
        if (ageMonths <= 24) return 0.3;
        if (ageMonths <= 36) return 0.6;
        return 0.9;
    }
    
    private double calculateIssueFactor(Device device) {
        return 0.2;
    }
    
    private double calculateMaintenanceFactor(Device device) {
        if (device.getLastMaintenanceDate() == null) return 0.5;
        long monthsSinceMaintenance = ChronoUnit.MONTHS.between(
            device.getLastMaintenanceDate(), LocalDate.now());
        return Math.min(0.9, monthsSinceMaintenance * 0.1);
    }
    
    private double calculateUsageFactor(Device device) {
        long assignmentCount = assignmentRepository.countAssignmentsByDevice(device.getId());
        return Math.min(0.9, assignmentCount * 0.05);
    }
    
    private String getRiskLevel(double probability) {
        if (probability < 0.3) return "LOW";
        if (probability < 0.6) return "MEDIUM";
        return "HIGH";
    }
    
    private LocalDate predictFailureDate(Device device, double probability) {
        if (probability < 0.3) return LocalDate.now().plusMonths(12);
        if (probability < 0.6) return LocalDate.now().plusMonths(6);
        return LocalDate.now().plusMonths(3);
    }
    
    private String getRecommendedAction(double probability) {
        if (probability < 0.3) return "Routine maintenance only";
        if (probability < 0.6) return "Schedule preventive maintenance";
        return "Immediate maintenance required - High risk of failure";
    }
    
    // ===== 2. ANOMALY DETECTION =====
    public List<Map<String, Object>> detectAnomalies() {
        List<AssignmentHistory> assignments = assignmentRepository.findAll();
        List<Map<String, Object>> anomalies = new ArrayList<>();
        
        double avgDuration = assignments.stream()
            .filter(a -> a.getActualReturnDate() != null)
            .mapToLong(a -> ChronoUnit.DAYS.between(
                a.getAssignedDate(), a.getActualReturnDate()))
            .average()
            .orElse(0);
        
        for (AssignmentHistory assignment : assignments) {
            if (assignment.getActualReturnDate() != null) {
                long duration = ChronoUnit.DAYS.between(
                    assignment.getAssignedDate(), assignment.getActualReturnDate());
                
                if (duration > avgDuration + 30) {
                    Map<String, Object> anomaly = new HashMap<>();
                    anomaly.put("type", "UNUSUALLY_LONG_ASSIGNMENT");
                    anomaly.put("assignmentId", assignment.getId());
                    anomaly.put("deviceId", assignment.getDeviceId());
                    anomaly.put("employeeId", assignment.getEmployeeId());
                    anomaly.put("duration", duration);
                    anomaly.put("expectedDuration", (long) avgDuration);
                    anomalies.add(anomaly);
                }
            }
        }
        
        return anomalies;
    }
    
    // ===== 3. SMART SEARCH =====
    public List<Device> smartSearch(String query) {
        String lowerQuery = query.toLowerCase();
        List<Device> allDevices = deviceRepository.findAll();
        Map<String, String> searchParams = parseNaturalLanguage(query);
        
        return allDevices.stream()
            .filter(device -> matchesSearch(device, searchParams, lowerQuery))
            .collect(Collectors.toList());
    }
    
    private Map<String, String> parseNaturalLanguage(String query) {
        Map<String, String> params = new HashMap<>();
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("laptop")) params.put("type", "LAPTOP");
        else if (lowerQuery.contains("desktop")) params.put("type", "DESKTOP");
        else if (lowerQuery.contains("mobile") || lowerQuery.contains("phone")) 
            params.put("type", "MOBILE");
        else if (lowerQuery.contains("tablet")) params.put("type", "TABLET");
        
        if (lowerQuery.contains("available")) params.put("status", "AVAILABLE");
        else if (lowerQuery.contains("assigned")) params.put("status", "ASSIGNED");
        else if (lowerQuery.contains("repair")) params.put("status", "UNDER_REPAIR");
        
        if (lowerQuery.contains("dell")) params.put("brand", "Dell");
        else if (lowerQuery.contains("hp")) params.put("brand", "HP");
        else if (lowerQuery.contains("apple")) params.put("brand", "Apple");
        else if (lowerQuery.contains("lenovo")) params.put("brand", "Lenovo");
        else if (lowerQuery.contains("samsung")) params.put("brand", "Samsung");
        
        return params;
    }
    
    private boolean matchesSearch(Device device, Map<String, String> params, String query) {
        if (params.containsKey("type") && !device.getDeviceType().equals(params.get("type")))
            return false;
        
        if (params.containsKey("status") && !device.getCurrentStatus().equals(params.get("status")))
            return false;
        
        if (params.containsKey("brand") && device.getBrand() != null && 
            !device.getBrand().equalsIgnoreCase(params.get("brand")))
            return false;
        
        return true;
    }
    
    // ===== 4. DEMAND FORECASTING =====
    public Map<String, Object> forecastDemand(int months) {
        List<AssignmentHistory> assignments = assignmentRepository.findAll();
        
        Map<String, Long> monthlyDemand = assignments.stream()
            .filter(a -> a.getAssignedDate() != null)
            .collect(Collectors.groupingBy(
                a -> a.getAssignedDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.counting()
            ));
        
        List<Double> historicalData = monthlyDemand.values().stream()
            .map(Long::doubleValue)
            .collect(Collectors.toList());
        
        double forecast = simpleLinearForecast(historicalData, months);
        
        Map<String, Object> forecastResult = new HashMap<>();
        forecastResult.put("historicalDemand", monthlyDemand);
        forecastResult.put("forecastedDemand", Math.round(forecast));
        forecastResult.put("forecastPeriod", months + " months");
        forecastResult.put("recommendation", forecast > 10 ? "Increase stock" : "Maintain current levels");
        
        return forecastResult;
    }
    
    private double simpleLinearForecast(List<Double> data, int periods) {
        if (data.size() < 2) return data.isEmpty() ? 0 : data.get(0);
        
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < data.size(); i++) {
            sumX += i;
            sumY += data.get(i);
            sumXY += i * data.get(i);
            sumX2 += i * i;
        }
        
        double n = data.size();
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        
        return intercept + slope * (data.size() + periods - 1);
    }
    
    // ===== 5. AI CHATBOT RESPONSE =====
    public String getChatbotResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
            return "Hello! I'm your Airtel Inventory Assistant. How can I help you today?";
        }
        
        if (lowerMessage.contains("device") && lowerMessage.contains("assign")) {
            return "To assign a device, go to the Assignments page, select a device and employee, then click 'Assign Device'.";
        }
        
        if (lowerMessage.contains("how many") && lowerMessage.contains("device")) {
            long totalDevices = deviceRepository.count();
            long availableDevices = deviceRepository.countByStatus("AVAILABLE");
            long assignedDevices = deviceRepository.countByStatus("ASSIGNED");
            return String.format("We have %d total devices. %d are available and %d are currently assigned.",
                totalDevices, availableDevices, assignedDevices);
        }
        
        if (lowerMessage.contains("health") && lowerMessage.contains("device")) {
            Double avgHealth = deviceRepository.getAverageHealthScore();
            return String.format("The average device health score is %.1f%%. Devices with health below 40%% need attention.",
                avgHealth != null ? avgHealth : 0);
        }
        
        if (lowerMessage.contains("overdue")) {
            List<AssignmentHistory> overdue = assignmentRepository.findAll().stream()
                .filter(a -> a.getActualReturnDate() == null && a.getExpectedReturnDate() != null &&
                            LocalDate.now().isAfter(a.getExpectedReturnDate()))
                .collect(Collectors.toList());
            return String.format("There are %d overdue device assignments. Please check the Assignments page.",
                overdue.size());
        }
        
        if (lowerMessage.contains("help")) {
            return "I can help you with:\n- Device statistics\n- Assignment information\n- Device health status\n- Overdue alerts\n- How-to guides for common tasks";
        }
        
        return "I understand your question. For specific assistance, please contact your IT administrator or check the user manual.";
    }
}