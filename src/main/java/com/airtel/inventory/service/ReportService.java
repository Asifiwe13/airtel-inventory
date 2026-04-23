package com.airtel.inventory.service;

import com.airtel.inventory.model.Device;
import com.airtel.inventory.model.Employee;
import com.airtel.inventory.model.AssignmentHistory;
import com.airtel.inventory.repository.DeviceRepository;
import com.airtel.inventory.repository.EmployeeRepository;
import com.airtel.inventory.repository.AssignmentHistoryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
    
    private final DeviceRepository deviceRepository;
    private final EmployeeRepository employeeRepository;
    private final AssignmentHistoryRepository assignmentRepository;
    
    public ReportService(DeviceRepository deviceRepository,
                         EmployeeRepository employeeRepository,
                         AssignmentHistoryRepository assignmentRepository) {
        this.deviceRepository = deviceRepository;
        this.employeeRepository = employeeRepository;
        this.assignmentRepository = assignmentRepository;
    }
    
    // ===== EXCEL EXPORTS =====
    
    public byte[] exportDevicesToExcel() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Devices");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        Row header = sheet.createRow(0);
        String[] columns = {"Asset Tag", "Serial Number", "Type", "Brand", "Model", 
                           "Status", "Health Score", "Condition", "Purchase Date", 
                           "Warranty End", "Cost (₹)", "Current Owner"};
        
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000);
        }
        
        List<Device> devices = deviceRepository.findAll();
        int rowNum = 1;
        for (Device device : devices) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(device.getAssetTag());
            row.createCell(1).setCellValue(device.getSerialNumber());
            row.createCell(2).setCellValue(device.getDeviceType());
            row.createCell(3).setCellValue(device.getBrand() != null ? device.getBrand() : "");
            row.createCell(4).setCellValue(device.getModel() != null ? device.getModel() : "");
            row.createCell(5).setCellValue(device.getCurrentStatus());
            row.createCell(6).setCellValue(device.getHealthScore());
            row.createCell(7).setCellValue(device.getConditionStatus());
            row.createCell(8).setCellValue(device.getPurchaseDate() != null ? device.getPurchaseDate().toString() : "");
            row.createCell(9).setCellValue(device.getWarrantyEnd() != null ? device.getWarrantyEnd().toString() : "");
            row.createCell(10).setCellValue(device.getCost() != null ? device.getCost().doubleValue() : 0);
            row.createCell(11).setCellValue(device.getCurrentOwnerId() != null ? device.getCurrentOwnerId() : "None");
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    public byte[] exportEmployeesToExcel() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        Row header = sheet.createRow(0);
        String[] columns = {"Employee ID", "Name", "Department", "Designation", "Email", "Phone", "Status"};
        
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000);
        }
        
        List<Employee> employees = employeeRepository.findAll();
        int rowNum = 1;
        for (Employee emp : employees) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(emp.getId());
            row.createCell(1).setCellValue(emp.getName());
            row.createCell(2).setCellValue(emp.getDepartment() != null ? emp.getDepartment() : "");
            row.createCell(3).setCellValue(emp.getDesignation() != null ? emp.getDesignation() : "");
            row.createCell(4).setCellValue(emp.getEmail() != null ? emp.getEmail() : "");
            row.createCell(5).setCellValue(emp.getPhone() != null ? emp.getPhone() : "");
            row.createCell(6).setCellValue(emp.getIsActive() ? "Active" : "Inactive");
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    public byte[] exportAssignmentsToExcel() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Assignments");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        Row header = sheet.createRow(0);
        String[] columns = {"Assignment ID", "Device ID", "Employee ID", "Assigned Date", 
                           "Expected Return", "Actual Return", "Condition", "Status", "Anomaly"};
        
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000);
        }
        
        List<AssignmentHistory> assignments = assignmentRepository.findAll();
        int rowNum = 1;
        for (AssignmentHistory assignment : assignments) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(assignment.getId());
            row.createCell(1).setCellValue(assignment.getDeviceId());
            row.createCell(2).setCellValue(assignment.getEmployeeId());
            row.createCell(3).setCellValue(assignment.getAssignedDate() != null ? assignment.getAssignedDate().toString() : "");
            row.createCell(4).setCellValue(assignment.getExpectedReturnDate() != null ? 
                                          assignment.getExpectedReturnDate().toString() : "");
            row.createCell(5).setCellValue(assignment.getActualReturnDate() != null ? 
                                          assignment.getActualReturnDate().toString() : "Active");
            row.createCell(6).setCellValue(assignment.getConditionAtAssignment() != null ? assignment.getConditionAtAssignment() : "");
            row.createCell(7).setCellValue(assignment.getActualReturnDate() == null ? "Active" : "Returned");
            row.createCell(8).setCellValue(assignment.getIsAnomaly() != null && assignment.getIsAnomaly() ? "Yes" : "No");
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    // ===== SUMMARY REPORTS =====
    
    public ReportSummary getSummaryReport() {
        ReportSummary summary = new ReportSummary();
        
        summary.totalDevices = deviceRepository.count();
        summary.availableDevices = deviceRepository.countByStatus("AVAILABLE");
        summary.assignedDevices = deviceRepository.countByStatus("ASSIGNED");
        summary.underRepair = deviceRepository.countByStatus("UNDER_REPAIR");
        summary.totalEmployees = employeeRepository.count();
        summary.activeEmployees = employeeRepository.findByIsActiveTrue().size();
        summary.totalAssignments = assignmentRepository.count();
        
        List<AssignmentHistory> allAssignments = assignmentRepository.findAll();
        long activeAssignments = allAssignments.stream()
            .filter(a -> a.getActualReturnDate() == null)
            .count();
        summary.activeAssignments = activeAssignments;
        
        Double avgHealth = deviceRepository.getAverageHealthScore();
        summary.averageHealthScore = avgHealth != null ? avgHealth : 0.0;
        summary.highRiskDevices = deviceRepository.findByHealthScoreLessThan(40).size();
        
        return summary;
    }
    
    public Map<String, Object> getReportByDepartment() {
        List<Employee> employees = employeeRepository.findAll();
        Map<String, Long> departmentCount = employees.stream()
            .filter(e -> e.getDepartment() != null)
            .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
        Map<String, Object> result = new HashMap<>();
        result.put("data", departmentCount);
        return result;
    }
    
    public Map<String, Object> getReportByDeviceType() {
        List<Device> devices = deviceRepository.findAll();
        Map<String, Long> typeCount = devices.stream()
            .collect(Collectors.groupingBy(Device::getDeviceType, Collectors.counting()));
        Map<String, Object> result = new HashMap<>();
        result.put("data", typeCount);
        return result;
    }
    
    public Map<String, Object> getReportByStatus() {
        Map<String, Object> statusReport = new HashMap<>();
        statusReport.put("AVAILABLE", deviceRepository.countByStatus("AVAILABLE"));
        statusReport.put("ASSIGNED", deviceRepository.countByStatus("ASSIGNED"));
        statusReport.put("UNDER_REPAIR", deviceRepository.countByStatus("UNDER_REPAIR"));
        return statusReport;
    }
    
    public Map<String, Object> getReportByDateRange(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        
        long devicesAdded = deviceRepository.findAll().stream()
            .filter(d -> d.getCreatedAt() != null && 
                        d.getCreatedAt().toLocalDate().isAfter(startDate) &&
                        d.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)))
            .count();
        report.put("devicesAdded", devicesAdded);
        
        return report;
    }
    
    // ===== HELPER METHODS =====
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    // ===== INNER CLASS =====
    
    public static class ReportSummary {
        private long totalDevices;
        private long availableDevices;
        private long assignedDevices;
        private long underRepair;
        private long totalEmployees;
        private long activeEmployees;
        private long totalAssignments;
        private long activeAssignments;
        private double averageHealthScore;
        private int highRiskDevices;
        
        public long getTotalDevices() { return totalDevices; }
        public long getAvailableDevices() { return availableDevices; }
        public long getAssignedDevices() { return assignedDevices; }
        public long getUnderRepair() { return underRepair; }
        public long getTotalEmployees() { return totalEmployees; }
        public long getActiveEmployees() { return activeEmployees; }
        public long getTotalAssignments() { return totalAssignments; }
        public long getActiveAssignments() { return activeAssignments; }
        public double getAverageHealthScore() { return averageHealthScore; }
        public int getHighRiskDevices() { return highRiskDevices; }
        
        public void setTotalDevices(long totalDevices) { this.totalDevices = totalDevices; }
        public void setAvailableDevices(long availableDevices) { this.availableDevices = availableDevices; }
        public void setAssignedDevices(long assignedDevices) { this.assignedDevices = assignedDevices; }
        public void setUnderRepair(long underRepair) { this.underRepair = underRepair; }
        public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }
        public void setActiveEmployees(long activeEmployees) { this.activeEmployees = activeEmployees; }
        public void setTotalAssignments(long totalAssignments) { this.totalAssignments = totalAssignments; }
        public void setActiveAssignments(long activeAssignments) { this.activeAssignments = activeAssignments; }
        public void setAverageHealthScore(double averageHealthScore) { this.averageHealthScore = averageHealthScore; }
        public void setHighRiskDevices(int highRiskDevices) { this.highRiskDevices = highRiskDevices; }
    }
}