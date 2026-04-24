package com.airtel.inventory.service;

import com.airtel.inventory.model.Device;
import com.airtel.inventory.model.Employee;
import com.airtel.inventory.model.AssignmentHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.email.admin:admin@airtel.com}")
    private String adminEmail;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendDeviceAssignmentNotification(Device device, Employee employee, AssignmentHistory assignment) {
        if (!emailEnabled) {
            log.info("EMAIL DISABLED - Would send assignment notification to: {}", employee.getEmail());
            return;
        }
        try {
            String subject = "Device Assigned - " + device.getAssetTag();
            String body = buildAssignmentEmailBody(device, employee, assignment);
            sendEmail(employee.getEmail(), subject, body);
            log.info("Assignment notification sent to: {}", employee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send assignment email to {}: {}", employee.getEmail(), e.getMessage());
        }
    }

    public void sendDeviceReturnNotification(Device device, Employee employee, AssignmentHistory assignment) {
        if (!emailEnabled) {
            log.info("EMAIL DISABLED - Would send return notification to: {}", employee.getEmail());
            return;
        }
        try {
            String subject = "Device Returned - " + device.getAssetTag();
            String body = buildReturnEmailBody(device, employee, assignment);
            sendEmail(employee.getEmail(), subject, body);
            log.info("Return confirmation sent to: {}", employee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send return email to {}: {}", employee.getEmail(), e.getMessage());
        }
    }

    public void sendOverdueAlert(Device device, Employee employee, AssignmentHistory assignment, long daysOverdue) {
        if (!emailEnabled) {
            log.info("EMAIL DISABLED - Would send overdue alert to: {}", employee.getEmail());
            return;
        }
        try {
            String subject = "URGENT: Device Overdue - " + device.getAssetTag();
            String body = buildOverdueEmailBody(device, employee, assignment, daysOverdue);
            sendEmail(employee.getEmail(), subject, body);
            sendEmail(adminEmail, subject, body);
        } catch (Exception e) {
            log.error("Failed to send overdue alert: {}", e.getMessage());
        }
    }

    public void sendWelcomeEmail(Employee employee) {
        if (!emailEnabled) {
            log.info("EMAIL DISABLED - Would send welcome email to: {}", employee.getEmail());
            return;
        }
        try {
            String subject = "Welcome to Airtel Inventory System";
            String body = buildWelcomeEmailBody(employee);
            sendEmail(employee.getEmail(), subject, body);
        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage());
        }
    }

    public void sendDeviceRegistrationNotification(Device device) {
        if (!emailEnabled) {
            log.info("EMAIL DISABLED - Would send device registration notification to admin");
            return;
        }
        try {
            String subject = "New Device Registered - " + device.getAssetTag();
            String body = buildDeviceRegistrationEmailBody(device);
            sendEmail(adminEmail, subject, body);
        } catch (Exception e) {
            log.error("Failed to send device registration email: {}", e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String body) {
        if (mailSender == null) {
            log.warn("Mail sender not configured. Skipping email to: {}", to);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildAssignmentEmailBody(Device device, Employee employee, AssignmentHistory assignment) {
        return "<html><body style='font-family:Arial'>"
            + "<h2 style='color:#dc2626'>Device Assignment Confirmation</h2>"
            + "<p>Dear <strong>" + employee.getName() + "</strong>,</p>"
            + "<p>Device <strong>" + device.getAssetTag() + "</strong> has been assigned to you.</p>"
            + "<table border='1' cellpadding='8'>"
            + "<tr><td>Asset Tag</td><td>" + device.getAssetTag() + "</td></tr>"
            + "<tr><td>Serial Number</td><td>" + device.getSerialNumber() + "</td></tr>"
            + "<tr><td>Type</td><td>" + device.getDeviceType() + "</td></tr>"
            + "<tr><td>Brand/Model</td><td>" + (device.getBrand() != null ? device.getBrand() : "") + " " + (device.getModel() != null ? device.getModel() : "") + "</td></tr>"
            + "<tr><td>Assigned Date</td><td>" + assignment.getAssignedDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")) + "</td></tr>"
            + "<tr><td>Expected Return</td><td>" + (assignment.getExpectedReturnDate() != null ? assignment.getExpectedReturnDate().toString() : "Not specified") + "</td></tr>"
            + "</table>"
            + "<p>Please take care of this device and return it on time.</p>"
            + "<p style='color:#6b7280;font-size:12px'>Airtel Inventory System - Automated Message</p>"
            + "</body></html>";
    }

    private String buildReturnEmailBody(Device device, Employee employee, AssignmentHistory assignment) {
        return "<html><body style='font-family:Arial'>"
            + "<h2 style='color:#10b981'>Device Return Confirmation</h2>"
            + "<p>Dear <strong>" + employee.getName() + "</strong>,</p>"
            + "<p>Device <strong>" + device.getAssetTag() + "</strong> has been returned successfully.</p>"
            + "<table border='1' cellpadding='8'>"
            + "<tr><td>Asset Tag</td><td>" + device.getAssetTag() + "</td></tr>"
            + "<tr><td>Return Date</td><td>" + assignment.getActualReturnDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")) + "</td></tr>"
            + "<tr><td>Condition</td><td>" + (assignment.getConditionAtReturn() != null ? assignment.getConditionAtReturn() : "GOOD") + "</td></tr>"
            + "</table>"
            + "<p>Thank you for returning the device.</p>"
            + "<p style='color:#6b7280;font-size:12px'>Airtel Inventory System - Automated Message</p>"
            + "</body></html>";
    }

    private String buildOverdueEmailBody(Device device, Employee employee, AssignmentHistory assignment, long daysOverdue) {
        return "<html><body style='font-family:Arial'>"
            + "<h2 style='color:#ef4444'>URGENT: Device Overdue Alert</h2>"
            + "<p>Dear <strong>" + employee.getName() + "</strong>,</p>"
            + "<p>Device <strong>" + device.getAssetTag() + "</strong> is <strong style='color:red'>" + daysOverdue + " days overdue</strong>!</p>"
            + "<p>Please return this device immediately.</p>"
            + "<p style='color:#6b7280;font-size:12px'>Airtel Inventory System - Automated Message</p>"
            + "</body></html>";
    }

    private String buildWelcomeEmailBody(Employee employee) {
        return "<html><body style='font-family:Arial'>"
            + "<h2 style='color:#dc2626'>Welcome to Airtel Inventory System</h2>"
            + "<p>Dear <strong>" + employee.getName() + "</strong>,</p>"
            + "<p>You have been registered in the Airtel Inventory Management System.</p>"
            + "<p>You will receive notifications for device assignments and returns.</p>"
            + "<p style='color:#6b7280;font-size:12px'>Airtel Inventory System - Automated Message</p>"
            + "</body></html>";
    }

    private String buildDeviceRegistrationEmailBody(Device device) {
        return "<html><body style='font-family:Arial'>"
            + "<h2 style='color:#3b82f6'>New Device Registered</h2>"
            + "<p>A new device has been added to inventory:</p>"
            + "<table border='1' cellpadding='8'>"
            + "<tr><td>Asset Tag</td><td>" + device.getAssetTag() + "</td></tr>"
            + "<tr><td>Serial Number</td><td>" + device.getSerialNumber() + "</td></tr>"
            + "<tr><td>Type</td><td>" + device.getDeviceType() + "</td></tr>"
            + "<tr><td>Brand</td><td>" + (device.getBrand() != null ? device.getBrand() : "N/A") + "</td></tr>"
            + "</table>"
            + "<p style='color:#6b7280;font-size:12px'>Airtel Inventory System - Automated Message</p>"
            + "</body></html>";
    }
}
