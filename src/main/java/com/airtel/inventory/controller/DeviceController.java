package com.airtel.inventory.controller;

import com.airtel.inventory.dto.DeviceDTO;
import com.airtel.inventory.model.Device;
import com.airtel.inventory.service.DeviceService;
import com.airtel.inventory.service.QRCodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*")
public class DeviceController {

    private final DeviceService deviceService;
    private final QRCodeService qrCodeService;

    public DeviceController(DeviceService deviceService, QRCodeService qrCodeService) {
        this.deviceService = deviceService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping
    public ResponseEntity<?> registerDevice(@Valid @RequestBody DeviceDTO deviceDTO) {
        try {
            Device device = deviceService.registerDevice(deviceDTO);
            return new ResponseEntity<>(device, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        return ResponseEntity.ok(deviceService.getStatistics());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> getDeviceQRCode(@PathVariable Long id) {
        try {
            Device device = deviceService.getDeviceById(id);
            String tmpDir = System.getProperty("java.io.tmpdir");
            String qrPath = tmpDir + "/qrcodes/device_" + id + "_qr.png";

            java.io.File qrFile = new java.io.File(qrPath);
            if (!qrFile.exists()) {
                qrCodeService.generateQRCode(device);
            }

            byte[] imageBytes = Files.readAllBytes(Paths.get(qrPath));
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/barcode")
    public ResponseEntity<byte[]> getDeviceBarcode(@PathVariable Long id) {
        try {
            Device device = deviceService.getDeviceById(id);
            String tmpDir = System.getProperty("java.io.tmpdir");
            String barcodePath = tmpDir + "/qrcodes/device_" + id + "_barcode.png";

            java.io.File barcodeFile = new java.io.File(barcodePath);
            if (!barcodeFile.exists()) {
                qrCodeService.generateBarcode(device);
            }

            byte[] imageBytes = Files.readAllBytes(Paths.get(barcodePath));
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
