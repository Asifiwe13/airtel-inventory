package com.airtel.inventory.controller;

import com.airtel.inventory.dto.EmployeeDTO;
import com.airtel.inventory.model.Employee;
import com.airtel.inventory.model.AssignmentHistory;
import com.airtel.inventory.service.EmployeeService;
import com.airtel.inventory.repository.AssignmentHistoryRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    private final AssignmentHistoryRepository assignmentHistoryRepository;  // ← ADD THIS
    
    // Update constructor to include the new repository
    public EmployeeController(EmployeeService employeeService, 
                              AssignmentHistoryRepository assignmentHistoryRepository) {
        this.employeeService = employeeService;
        this.assignmentHistoryRepository = assignmentHistoryRepository;  // ← ADD THIS
    }
    
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }
    
    @PostMapping
    public ResponseEntity<?> registerEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        try {
            Employee employee = new Employee();
            employee.setId(employeeDTO.getId());
            employee.setName(employeeDTO.getName());
            employee.setDepartment(employeeDTO.getDepartment());
            employee.setDesignation(employeeDTO.getDesignation());
            employee.setEmail(employeeDTO.getEmail());
            employee.setPhone(employeeDTO.getPhone());
            employee.setIsActive(true);
            
            Employee savedEmployee = employeeService.registerEmployee(employee);
            return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable String id, @Valid @RequestBody EmployeeDTO employeeDTO) {
        try {
            Employee employeeDetails = new Employee();
            employeeDetails.setName(employeeDTO.getName());
            employeeDetails.setDepartment(employeeDTO.getDepartment());
            employeeDetails.setDesignation(employeeDTO.getDesignation());
            employeeDetails.setEmail(employeeDTO.getEmail());
            employeeDetails.setPhone(employeeDTO.getPhone());
            
            Employee updatedEmployee = employeeService.updateEmployee(id, employeeDetails);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Employee>> getActiveEmployees() {
        return ResponseEntity.ok(employeeService.getActiveEmployees());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployees(@RequestParam String keyword) {
        return ResponseEntity.ok(employeeService.searchEmployees(keyword));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable String id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok().body("Employee deleted successfully");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting employee: " + e.getMessage(), 
                                       HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Check if employee has active assignments
    @GetMapping("/{id}/has-assignments")
    public ResponseEntity<Boolean> hasActiveAssignments(@PathVariable String id) {
        List<AssignmentHistory> activeAssignments = 
            assignmentHistoryRepository.findCurrentAssignmentsByEmployee(id);
        return ResponseEntity.ok(!activeAssignments.isEmpty());
    }
}