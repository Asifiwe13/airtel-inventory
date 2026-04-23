package com.airtel.inventory.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airtel.inventory.model.AssignmentHistory;
import com.airtel.inventory.model.Device;
import com.airtel.inventory.model.Employee;
import com.airtel.inventory.service.AssignmentService;
import com.airtel.inventory.service.DeviceService;
import com.airtel.inventory.service.EmployeeService;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "*")
public class AssignmentController {
    
    private final AssignmentService assignmentService;
    private final DeviceService deviceService;
    private final EmployeeService employeeService;
    
    public AssignmentController(AssignmentService assignmentService,
                                DeviceService deviceService,
                                EmployeeService employeeService) {
        this.assignmentService = assignmentService;
        this.deviceService = deviceService;
        this.employeeService = employeeService;
    }
    
    @PostMapping("/assign")
    public ResponseEntity<?> assignDevice(@RequestBody Map<String, Object> request) {
        try {
            Long deviceId = Long.valueOf(request.get("deviceId").toString());
            String employeeId = request.get("employeeId").toString();
            LocalDate expectedReturnDate = request.get("expectedReturnDate") != null ? 
                LocalDate.parse(request.get("expectedReturnDate").toString()) : null;
            String condition = request.get("conditionAtAssignment") != null ? 
                request.get("conditionAtAssignment").toString() : "GOOD";
            String remarks = request.get("remarks") != null ? 
                request.get("remarks").toString() : "";
            
            AssignmentHistory assignment = assignmentService.assignDevice(
                deviceId, employeeId, expectedReturnDate, condition, remarks
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Device assigned successfully");
            response.put("assignment", assignment);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/return/{assignmentId}")
    public ResponseEntity<?> returnDevice(@PathVariable Long assignmentId,
                                          @RequestBody Map<String, String> request) {
        try {
            String condition = request.get("conditionAtReturn");
            String remarks = request.get("remarks");
            
            AssignmentHistory assignment = assignmentService.returnDevice(
                assignmentId, condition, remarks
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Device returned successfully");
            response.put("assignment", assignment);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<AssignmentHistory>> getDeviceHistory(@PathVariable Long deviceId) {
        return ResponseEntity.ok(assignmentService.getDeviceAssignmentHistory(deviceId));
    }
    
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AssignmentHistory>> getEmployeeHistory(@PathVariable String employeeId) {
        return ResponseEntity.ok(assignmentService.getEmployeeAssignmentHistory(employeeId));
    }
    
    @GetMapping("/current")
    public ResponseEntity<List<AssignmentHistory>> getCurrentAssignments() {
        return ResponseEntity.ok(assignmentService.getCurrentAssignments());
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<AssignmentHistory>> getOverdueAssignments() {
        return ResponseEntity.ok(assignmentService.getOverdueAssignments());
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(assignmentService.getAssignmentStatistics());
    }
    
    @GetMapping("/available-devices")
    public ResponseEntity<List<Device>> getAvailableDevices() {
        return ResponseEntity.ok(deviceService.getAvailableDevices());
    }
    
    @GetMapping("/active-employees")
    public ResponseEntity<List<Employee>> getActiveEmployees() {
        return ResponseEntity.ok(employeeService.getActiveEmployees());
    }
}