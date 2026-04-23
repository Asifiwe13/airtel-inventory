package com.airtel.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InventoryApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
        System.out.println("========================================");
        System.out.println("AIRTEL INVENTORY SYSTEM IS RUNNING!");
        System.out.println("Access: http://localhost:8080");
        System.out.println("Login: admin / admin123");
        System.out.println("========================================");
    }
}