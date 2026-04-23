package com.airtel.inventory.service;

import com.airtel.inventory.model.Device;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.oned.Code128Writer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class QRCodeService {
    
    @Value("${app.qr.storage.path:C:/airtel_inventory/qrcodes}")
    private String qrStoragePath;
    
    // Generate QR Code
    public String generateQRCode(Device device) {
        try {
            createDirectoryIfNotExists();
            
            String qrContent = String.format(
                "Device: %s\nType: %s\nBrand: %s\nModel: %s\nStatus: %s\nSN: %s",
                device.getAssetTag(),
                device.getDeviceType(),
                device.getBrand() != null ? device.getBrand() : "N/A",
                device.getModel() != null ? device.getModel() : "N/A",
                device.getCurrentStatus(),
                device.getSerialNumber()
            );
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 400, 400);
            
            String filePath = qrStoragePath + "/device_" + device.getId() + "_qr.png";
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            
            log.info("✅ QR Code generated for device: {}", device.getAssetTag());
            return filePath;
            
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code: {}", e.getMessage());
            return null;
        }
    }
    
    // Generate Barcode (Code 128) - FIXED
    public String generateBarcode(Device device) {
        try {
            createDirectoryIfNotExists();
            
            String barcodeContent = device.getAssetTag();
            
            Code128Writer barcodeWriter = new Code128Writer();
            BitMatrix bitMatrix = barcodeWriter.encode(
                barcodeContent, 
                BarcodeFormat.CODE_128, 
                400, 
                100
            );
            
            String filePath = qrStoragePath + "/device_" + device.getId() + "_barcode.png";
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            
            log.info("✅ Barcode generated for device: {}", device.getAssetTag());
            return filePath;
            
        } catch (Exception e) {  // ✅ Catch all exceptions instead of specific ones
            log.error("Failed to generate barcode: {}", e.getMessage());
            return null;
        }
    }
    
    private void createDirectoryIfNotExists() throws IOException {
        Path directory = Path.of(qrStoragePath);
        if (!directory.toFile().exists()) {
            Files.createDirectories(directory);
            log.info("Created QR code directory: {}", qrStoragePath);
        }
    }
}