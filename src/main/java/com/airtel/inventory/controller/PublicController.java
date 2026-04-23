package com.airtel.inventory.controller;

import com.airtel.inventory.model.Device;
import com.airtel.inventory.model.AssignmentHistory;
import com.airtel.inventory.model.Employee;
import com.airtel.inventory.service.DeviceService;
import com.airtel.inventory.service.AssignmentService;
import com.airtel.inventory.repository.AssignmentHistoryRepository;
import com.airtel.inventory.repository.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicController {
    
    private final DeviceService deviceService;
    private final AssignmentHistoryRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    
    public PublicController(DeviceService deviceService,
                            AssignmentHistoryRepository assignmentRepository,
                            EmployeeRepository employeeRepository) {
        this.deviceService = deviceService;
        this.assignmentRepository = assignmentRepository;
        this.employeeRepository = employeeRepository;
    }
    
    @GetMapping("/device/{assetTag}")
    public ResponseEntity<?> getDeviceByAssetTag(@PathVariable String assetTag) {
        try {
            Device device = deviceService.getDeviceByAssetTag(assetTag);
            
            // Return limited info for public view
            Map<String, Object> publicDevice = new HashMap<>();
            publicDevice.put("id", device.getId());
            publicDevice.put("assetTag", device.getAssetTag());
            publicDevice.put("serialNumber", device.getSerialNumber());
            publicDevice.put("deviceType", device.getDeviceType());
            publicDevice.put("brand", device.getBrand());
            publicDevice.put("model", device.getModel());
            publicDevice.put("currentStatus", device.getCurrentStatus());
            publicDevice.put("healthScore", device.getHealthScore());
            publicDevice.put("specifications", device.getSpecifications());
            
            return ResponseEntity.ok(publicDevice);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/device/{deviceId}/current-assignment")
    public ResponseEntity<?> getCurrentAssignment(@PathVariable Long deviceId) {
        try {
            AssignmentHistory assignment = assignmentRepository
                .findCurrentAssignmentByDevice(deviceId)
                .orElse(null);
            
            if (assignment == null) {
                return ResponseEntity.ok(Map.of("status", "AVAILABLE"));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("assignedDate", assignment.getAssignedDate());
            result.put("expectedReturnDate", assignment.getExpectedReturnDate());
            result.put("actualReturnDate", assignment.getActualReturnDate());
            
            // Get employee info
            employeeRepository.findById(assignment.getEmployeeId())
                .ifPresent(emp -> {
                    result.put("employeeName", emp.getName());
                    result.put("employeeDepartment", emp.getDepartment());
                });
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "AVAILABLE"));
        }
    }
}