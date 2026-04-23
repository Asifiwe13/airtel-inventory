package com.airtel.inventory.service;

import com.airtel.inventory.model.Employee;
import com.airtel.inventory.repository.EmployeeRepository;
import com.airtel.inventory.repository.AssignmentHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    
    private final EmployeeRepository employeeRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;
    private final EmailService emailService;  // ✅ ADDED
    
    public EmployeeService(EmployeeRepository employeeRepository, 
                           AssignmentHistoryRepository assignmentHistoryRepository,
                           EmailService emailService) {
        this.employeeRepository = employeeRepository;
        this.assignmentHistoryRepository = assignmentHistoryRepository;
        this.emailService = emailService;
    }
    
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    public Employee getEmployeeById(String id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
    }
    
    @Transactional
    public Employee registerEmployee(Employee employee) {
        Employee savedEmployee = employeeRepository.save(employee);
        
        // ✅ Send welcome email to new employee
        emailService.sendWelcomeEmail(savedEmployee);
        
        log.info("Registered new employee: {} ({})", employee.getName(), employee.getId());
        return savedEmployee;
    }
    
    public List<Employee> getActiveEmployees() {
        return employeeRepository.findByIsActiveTrue();
    }
    
    public Employee updateEmployee(String id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);
        employee.setName(employeeDetails.getName());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setDesignation(employeeDetails.getDesignation());
        employee.setEmail(employeeDetails.getEmail());
        employee.setPhone(employeeDetails.getPhone());
        return employeeRepository.save(employee);
    }
    
    @Transactional
    public void deleteEmployee(String id) {
        Employee employee = getEmployeeById(id);
        
        long activeAssignments = assignmentHistoryRepository
            .findCurrentAssignmentsByEmployee(id).size();
        
        if (activeAssignments > 0) {
            throw new RuntimeException(
                "Cannot delete employee with " + activeAssignments + 
                " active device assignments. Please return all devices first."
            );
        }
        
        List<com.airtel.inventory.model.AssignmentHistory> assignments = 
            assignmentHistoryRepository.findByEmployeeId(id);
        assignmentHistoryRepository.deleteAll(assignments);
        
        employeeRepository.deleteById(id);
        log.info("Deleted employee: {} along with {} assignment records", id, assignments.size());
    }
    
    public List<Employee> searchEmployees(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        List<Employee> allEmployees = employeeRepository.findAll();
        return allEmployees.stream()
            .filter(e -> e.getName().toLowerCase().contains(lowerKeyword) ||
                        e.getId().toLowerCase().contains(lowerKeyword) ||
                        (e.getDepartment() != null && e.getDepartment().toLowerCase().contains(lowerKeyword)))
            .collect(Collectors.toList());
    }
}