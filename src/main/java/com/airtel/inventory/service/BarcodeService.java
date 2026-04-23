package com.airtel.inventory.service;

import com.airtel.inventory.model.Device;
import com.airtel.inventory.repository.DeviceRepository;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Service
public class BarcodeService {
    
    private static final Logger log = LoggerFactory.getLogger(BarcodeService.class);
    private final DeviceRepository deviceRepository;
    
    public BarcodeService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    
    public String decodeBarcode(MultipartFile imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile.getInputStream());
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, java.util.EnumSet.allOf(BarcodeFormat.class));
            
            Result result = new MultiFormatReader().decode(bitmap, hints);
            log.info("Barcode decoded: {}", result.getText());
            return result.getText();
            
        } catch (Exception e) {
            log.error("Failed to decode barcode: {}", e.getMessage());
            return null;
        }
    }
    
    public Device findDeviceByBarcode(String barcode) {
        // Search by asset tag or serial number
        return deviceRepository.findByAssetTag(barcode)
            .or(() -> deviceRepository.findBySerialNumber(barcode))
            .orElse(null);
    }
}