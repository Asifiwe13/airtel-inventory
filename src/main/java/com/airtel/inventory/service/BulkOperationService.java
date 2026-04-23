package com.airtel.inventory.service;

import com.airtel.inventory.model.Device;
import com.airtel.inventory.repository.DeviceRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

@Service
public class BulkOperationService {
    
    private final DeviceRepository deviceRepository;
    
    public BulkOperationService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    
    public List<String> importDevicesFromExcel(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = 0;
            
            for (Row row : sheet) {
                if (rowNum == 0) { rowNum++; continue; } // Skip header
                
                try {
                    Device device = new Device();
                    device.setAssetTag(getCellValue(row.getCell(0)));
                    device.setSerialNumber(getCellValue(row.getCell(1)));
                    device.setDeviceType(getCellValue(row.getCell(2)));
                    device.setBrand(getCellValue(row.getCell(3)));
                    device.setModel(getCellValue(row.getCell(4)));
                    device.setCurrentStatus("AVAILABLE");
                    device.setHealthScore(100);
                    
                    deviceRepository.save(device);
                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                }
                rowNum++;
            }
        } catch (Exception e) {
            errors.add("File processing error: " + e.getMessage());
        }
        
        return errors;
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }
}