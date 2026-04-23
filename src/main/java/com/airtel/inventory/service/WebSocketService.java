package com.airtel.inventory.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Send real-time device updates to all connected clients
     */
    public void sendDeviceUpdate(String deviceId, String action, Object data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "DEVICE_" + action);
        message.put("deviceId", deviceId);
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/devices", message);
    }
    
    /**
     * Send real-time assignment updates to all connected clients
     */
    public void sendAssignmentUpdate(String employeeId, String action, Object data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ASSIGNMENT_" + action);
        message.put("employeeId", employeeId);
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/assignments", message);
    }
    
    /**
     * Send real-time statistics updates to all connected clients
     */
    public void sendStatisticsUpdate(Object statistics) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "STATISTICS_UPDATE");
        message.put("data", statistics);
        message.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/statistics", message);
    }
    
    /**
     * Send notification to a specific user
     */
    public void sendNotification(String userId, String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("type", type);
        notification.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }
    
    /**
     * Send broadcast notification to all connected clients
     */
    public void sendBroadcast(String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("type", type);
        notification.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}