package com.airtel.inventory.controller;

import com.airtel.inventory.model.Device;
import com.airtel.inventory.model.Employee;
import com.airtel.inventory.model.AssignmentHistory;
import com.airtel.inventory.service.ReportService;
import com.airtel.inventory.service.EmailService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {
    
    private final ReportService reportService;
    private final EmailService emailService;  // ✅ ADDED
    
    public ReportController(ReportService reportService, EmailService emailService) {
        this.reportService = reportService;
        this.emailService = emailService;
    }
    
    // ===== EXCEL EXPORTS =====
    
    @GetMapping("/export/devices")
    public ResponseEntity<byte[]> exportDevicesToExcel() {
        try {
            byte[] excelData = reportService.exportDevicesToExcel();
            
            String filename = "devices_export_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(excelData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/test-send")
    public ResponseEntity<String> testSendEmail() {
        try {
            Device device = new Device();
            device.setAssetTag("TEST-DEVICE-001");
            device.setSerialNumber("SN123456");
            device.setDeviceType("LAPTOP");
            device.setBrand("Dell");
            device.setModel("XPS 15");
            
            Employee employee = new Employee();
            employee.setName("Test User");
            employee.setEmail("your-personal-email@gmail.com");  // ← PUT YOUR EMAIL HERE
            
            AssignmentHistory assignment = new AssignmentHistory();
            assignment.setAssignedDate(LocalDateTime.now());
            assignment.setExpectedReturnDate(LocalDate.now().plusDays(7));
            assignment.setConditionAtAssignment("GOOD");
            
            emailService.sendDeviceAssignmentNotification(device, employee, assignment);
            
            return ResponseEntity.ok("✅ Email sent! Check your inbox (and spam folder)");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/export/employees")
    public ResponseEntity<byte[]> exportEmployeesToExcel() {
        try {
            byte[] excelData = reportService.exportEmployeesToExcel();
            
            String filename = "employees_export_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(excelData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/export/assignments")
    public ResponseEntity<byte[]> exportAssignmentsToExcel() {
        try {
            byte[] excelData = reportService.exportAssignmentsToExcel();
            
            String filename = "assignments_export_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(excelData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ===== SUMMARY REPORTS =====
    
    @GetMapping("/summary")
    public ResponseEntity<ReportService.ReportSummary> getSummaryReport() {
        return ResponseEntity.ok(reportService.getSummaryReport());
    }
    
    @GetMapping("/by-department")
    public ResponseEntity<Map<String, Object>> getReportByDepartment() {
        return ResponseEntity.ok(reportService.getReportByDepartment());
    }
    
    @GetMapping("/by-device-type")
    public ResponseEntity<Map<String, Object>> getReportByDeviceType() {
        return ResponseEntity.ok(reportService.getReportByDeviceType());
    }
    
    @GetMapping("/by-status")
    public ResponseEntity<Map<String, Object>> getReportByStatus() {
        return ResponseEntity.ok(reportService.getReportByStatus());
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getReportByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getReportByDateRange(startDate, endDate));
    }
    
    // ✅ TEST EMAIL ENDPOINT - Fixed
    @GetMapping("/test-email")
    public ResponseEntity<String> testEmail() {
        try {
            Device testDevice = new Device();
            testDevice.setAssetTag("TEST-001");
            testDevice.setSerialNumber("SN-TEST-001");
            testDevice.setDeviceType("LAPTOP");
            testDevice.setBrand("Dell");
            testDevice.setModel("XPS 15");
            
            Employee testEmployee = new Employee();
            testEmployee.setName("Test User");
            testEmployee.setEmail("your-email@gmail.com"); // CHANGE THIS TO YOUR EMAIL
            
            AssignmentHistory testAssignment = new AssignmentHistory();
            testAssignment.setAssignedDate(LocalDateTime.now());
            testAssignment.setExpectedReturnDate(LocalDate.now().plusDays(7));
            testAssignment.setConditionAtAssignment("GOOD");
            
            emailService.sendDeviceAssignmentNotification(testDevice, testEmployee, testAssignment);
            
            return ResponseEntity.ok("✅ Test email sent! Check your inbox (or console if email is disabled).");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }
}