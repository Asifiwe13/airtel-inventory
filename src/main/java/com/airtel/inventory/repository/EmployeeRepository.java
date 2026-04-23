package com.airtel.inventory.repository;

import com.airtel.inventory.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    
    Optional<Employee> findByEmail(String email);
    
    List<Employee> findByDepartment(String department);
    
    List<Employee> findByIsActiveTrue();
    
    @Query("SELECT e FROM Employee e WHERE e.department = :dept AND e.isActive = true")
    List<Employee> findActiveEmployeesByDepartment(String dept);
}