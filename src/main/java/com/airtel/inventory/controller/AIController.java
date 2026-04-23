package com.airtel.inventory.controller;

import com.airtel.inventory.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {
    
    private final AIService aiService;
    
    public AIController(AIService aiService) {
        this.aiService = aiService;
    }
    
    @GetMapping("/predict/{deviceId}")
    public ResponseEntity<Map<String, Object>> predictFailure(@PathVariable Long deviceId) {
        return ResponseEntity.ok(aiService.predictDeviceFailure(deviceId));
    }
    
    @GetMapping("/high-risk")
    public ResponseEntity<List<Map<String, Object>>> getHighRiskDevices() {
        return ResponseEntity.ok(aiService.getHighRiskDevices());
    }
    
    @GetMapping("/anomalies")
    public ResponseEntity<List<Map<String, Object>>> detectAnomalies() {
        return ResponseEntity.ok(aiService.detectAnomalies());
    }
    
    @GetMapping("/forecast")
    public ResponseEntity<Map<String, Object>> forecastDemand(@RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(aiService.forecastDemand(months));
    }
    
    @GetMapping("/smart-search")
    public ResponseEntity<?> smartSearch(@RequestParam String query) {
        return ResponseEntity.ok(aiService.smartSearch(query));
    }
    
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = aiService.getChatbotResponse(message);
        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/test-prediction")
    public ResponseEntity<Map<String, Object>> testPrediction(@RequestBody Map<String, Object> request) {
        try {
            String assetTag = (String) request.get("assetTag");
            int ageMonths = request.get("ageMonths") != null ? (int) request.get("ageMonths") : 12;
            int issueCount = request.get("issueCount") != null ? (int) request.get("issueCount") : 0;
            int maintenanceDelay = request.get("maintenanceDelay") != null ? (int) request.get("maintenanceDelay") : 0;
            int usageCount = request.get("usageCount") != null ? (int) request.get("usageCount") : 0;
            
            // Calculate factors
            double ageFactor = ageMonths <= 12 ? 0.1 : (ageMonths <= 24 ? 0.3 : (ageMonths <= 36 ? 0.6 : 0.9));
            double issueFactor = Math.min(0.9, issueCount * 0.15);
            double maintenanceFactor = maintenanceDelay == 0 ? 0.5 : Math.min(0.9, maintenanceDelay * 0.1);
            double usageFactor = Math.min(0.9, usageCount * 0.05);
            
            double failureProbability = (ageFactor * 0.3) + (issueFactor * 0.35) + 
                                         (maintenanceFactor * 0.2) + (usageFactor * 0.15);
            
            int failurePercent = (int) Math.round(failureProbability * 100);
            
            Map<String, Object> result = new HashMap<>();
            result.put("assetTag", assetTag);
            result.put("failureProbability", failurePercent);
            result.put("riskLevel", failurePercent < 30 ? "LOW" : (failurePercent < 60 ? "MEDIUM" : "HIGH"));
            result.put("ageFactor", Math.round(ageFactor * 100));
            result.put("issueFactor", Math.round(issueFactor * 100));
            result.put("maintenanceFactor", Math.round(maintenanceFactor * 100));
            result.put("usageFactor", Math.round(usageFactor * 100));
            result.put("recommendedAction", failurePercent < 30 ? "Routine maintenance only" : 
                                           (failurePercent < 60 ? "Schedule preventive maintenance" : 
                                           "Immediate maintenance required"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}