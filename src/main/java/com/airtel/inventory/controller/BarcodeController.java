package com.airtel.inventory.controller;

import com.airtel.inventory.model.Device;
import com.airtel.inventory.service.BarcodeService;
import com.airtel.inventory.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/barcode")
@CrossOrigin(origins = "*")
public class BarcodeController {
    
    private final BarcodeService barcodeService;
    private final DeviceService deviceService;
    
    public BarcodeController(BarcodeService barcodeService, DeviceService deviceService) {
        this.barcodeService = barcodeService;
        this.deviceService = deviceService;
    }
    
    @PostMapping("/scan")
    public ResponseEntity<?> scanBarcode(@RequestParam("image") MultipartFile image) {
        try {
            String barcode = barcodeService.decodeBarcode(image);
            if (barcode == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Could not decode barcode"));
            }
            
            Device device = barcodeService.findDeviceByBarcode(barcode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("barcode", barcode);
            response.put("device", device);
            response.put("found", device != null);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/device/{barcode}")
    public ResponseEntity<?> getDeviceByBarcode(@PathVariable String barcode) {
        Device device = barcodeService.findDeviceByBarcode(barcode);
        if (device == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(device);
    }
}