package com.airtel.inventory.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    
    @NotBlank(message = "Employee ID is required")
    private String id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String department;
    private String designation;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
}