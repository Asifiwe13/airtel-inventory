package com.airtel.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDTO {
    
    @NotBlank(message = "Asset tag is required")
    private String assetTag;
    
    @NotBlank(message = "Serial number is required")
    private String serialNumber;
    
    @NotBlank(message = "Device type is required")
    private String deviceType;
    
    private String brand;
    private String model;
    private String specifications;
    
    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;
    
    private LocalDate warrantyEnd;
    
    @Positive(message = "Cost must be positive")
    private BigDecimal cost;
}