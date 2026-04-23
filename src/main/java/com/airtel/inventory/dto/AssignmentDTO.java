package com.airtel.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDTO {
    
    @NotNull(message = "Device ID is required")
    private Long deviceId;
    
    @NotBlank(message = "Employee ID is required")
    private String employeeId;
    
    private LocalDate expectedReturnDate;
    private String conditionAtAssignment;
    private String remarks;
}