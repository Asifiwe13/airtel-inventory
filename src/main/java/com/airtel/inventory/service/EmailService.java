package com.airtel.inventory.service;

import com.airtel.inventory.model.Device;
import com.airtel.inventory.model.Employee;
import com.airtel.inventory.model.AssignmentHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    
    // ✅ ADD THIS LINE - The logger declaration was missing!
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${app.email.admin:admin@airtel.com}")
    private String adminEmail;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void sendDeviceAssignmentNotification(Device device, Employee employee, AssignmentHistory assignment) {
        if (!emailEnabled) {
            log.info("📧 EMAIL DISABLED - Would send assignment notification to: {}", employee.getEmail());
            logEmailContent(employee.getEmail(), buildAssignmentEmailBody(device, employee, assignment));
            return;
        }
        
        try {
            String subject = "📱 DEVICE ASSIGNED - " + device.getAssetTag();
            String body = buildAssignmentEmailBody(device, employee, assignment);
            
            sendEmail(employee.getEmail(), subject, body);
            log.info("✅ Assignment notification sent to: {}", employee.getEmail());
            
        } catch (Exception e) {
            log.error("❌ Failed to send assignment email to {}: {}", employee.getEmail(), e.getMessage());
        }
    }
    
    public void sendDeviceReturnNotification(Device device, Employee employee, AssignmentHistory assignment) {
        if (!emailEnabled) {
            log.info("📧 EMAIL DISABLED - Would send return notification to: {}", employee.getEmail());
            logEmailContent(employee.getEmail(), buildReturnEmailBody(device, employee, assignment));
            return;
        }
        
        try {
            String subject = "✅ DEVICE RETURNED - " + device.getAssetTag();
            String body = buildReturnEmailBody(device, employee, assignment);
            
            sendEmail(employee.getEmail(), subject, body);
            log.info("✅ Return confirmation sent to: {}", employee.getEmail());
            
        } catch (Exception e) {
            log.error("❌ Failed to send return email to {}: {}", employee.getEmail(), e.getMessage());
        }
    }
    
    public void sendOverdueAlert(Device device, Employee employee, AssignmentHistory assignment, long daysOverdue) {
        if (!emailEnabled) {
            log.info("📧 EMAIL DISABLED - Would send overdue alert to: {}", employee.getEmail());
            return;
        }
        
        try {
            String subject = "⚠️ URGENT: DEVICE OVERDUE - " + device.getAssetTag();
            String body = buildOverdueEmailBody(device, employee, assignment, daysOverdue);
            
            sendEmail(employee.getEmail(), subject, body);
            sendEmail(adminEmail, subject, body);
            log.info("✅ Overdue alert sent to: {} and admin", employee.getEmail());
            
        } catch (Exception e) {
            log.error("❌ Failed to send overdue alert: {}", e.getMessage());
        }
    }
    
    public void sendWelcomeEmail(Employee employee) {
        if (!emailEnabled) {
            log.info("📧 EMAIL DISABLED - Would send welcome email to: {}", employee.getEmail());
            return;
        }
        
        try {
            String subject = "🎉 Welcome to Airtel Inventory System";
            String body = buildWelcomeEmailBody(employee);
            
            sendEmail(employee.getEmail(), subject, body);
            log.info("✅ Welcome email sent to: {}", employee.getEmail());
            
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email: {}", e.getMessage());
        }
    }
    
    public void sendDeviceRegistrationNotification(Device device) {
        if (!emailEnabled) {
            log.info("📧 EMAIL DISABLED - Would send device registration notification to admin");
            return;
        }
        
        try {
            String subject = "🆕 NEW DEVICE REGISTERED - " + device.getAssetTag();
            String body = buildDeviceRegistrationEmailBody(device);
            
            sendEmail(adminEmail, subject, body);
            log.info("✅ Device registration notification sent to admin");
            
        } catch (Exception e) {
            log.error("❌ Failed to send device registration email: {}", e.getMessage());
        }
    }
    
    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            
            mailSender.send(message);
            log.info("📧 Email sent successfully to: {}", to);
            
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    private void logEmailContent(String to, String body) {
        log.info("========== EMAIL CONTENT (Not Sent - Email Disabled) ==========");
        log.info("To: {}", to);
        log.info("Body: {}", body);
        log.info("================================================================");
    }
    
    private String buildAssignmentEmailBody(Device device, Employee employee, AssignmentHistory assignment) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <style>\n");
        sb.append("        body { font-family: Arial, sans-serif; line-height: 1.6; }\n");
        sb.append("        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n");
        sb.append("        .header { background-color: #dc2626; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }\n");
        sb.append("        .header h2 { color: white; margin: 0; }\n");
        sb.append("        .content { padding: 20px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 10px 10px; }\n");
        sb.append("        .device-details { background-color: #f3f4f6; padding: 15px; border-radius: 8px; margin: 15px 0; }\n");
        sb.append("        .detail-row { margin: 10px 0; }\n");
        sb.append("        .label { font-weight: bold; color: #374151; }\n");
        sb.append("        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #e5e7eb; font-size: 12px; color: #6b7280; text-align: center; }\n");
        sb.append("        .status-badge { display: inline-block; background-color: #10b981; color: white; padding: 4px 12px; border-radius: 20px; font-size: 12px; }\n");
        sb.append("    </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("<div class=\"container\">\n");
        sb.append("    <div class=\"header\">\n");
        sb.append("        <h2>📱 Airtel Inventory System</h2>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"content\">\n");
        sb.append("        <h3>Device Assignment Confirmation</h3>\n");
        sb.append("        <p>Dear <strong>").append(employee.getName()).append("</strong>,</p>\n");
        sb.append("        <p>A device has been assigned to you. Please find the details below:</p>\n");
        sb.append("        <div class=\"device-details\">\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">🔖 Asset Tag:</span> <strong>").append(device.getAssetTag()).append("</strong>\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">📌 Serial Number:</span> ").append(device.getSerialNumber()).append("\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">💻 Device Type:</span> ").append(device.getDeviceType()).append("\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">🏭 Brand:</span> ").append(device.getBrand() != null ? device.getBrand() : "N/A").append("\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">🔧 Model:</span> ").append(device.getModel() != null ? device.getModel() : "N/A").append("\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">📅 Assigned Date:</span> ").append(assignment.getAssignedDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"))).append("\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">📆 Expected Return:</span> ").append(assignment.getExpectedReturnDate() != null ? assignment.getExpectedReturnDate().toString() : "Not specified").append("\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">💚 Device Condition:</span> <span class=\"status-badge\">").append(assignment.getConditionAtAssignment() != null ? assignment.getConditionAtAssignment() : "GOOD").append("</span>\n");
        sb.append("            </div>\n");
        sb.append("        </div>\n");
        sb.append("        <p><strong>Important Notes:</strong></p>\n");
        sb.append("        <ul>\n");
        sb.append("            <li>Please keep the device safe and secure</li>\n");
        sb.append("            <li>Report any issues immediately to IT support</li>\n");
        sb.append("            <li>Return the device on or before the expected return date</li>\n");
        sb.append("            <li>Do not share this device with unauthorized personnel</li>\n");
        sb.append("        </ul>\n");
        sb.append("        <p>If you have any questions, please contact your IT administrator.</p>\n");
        sb.append("        <div class=\"footer\">\n");
        sb.append("            <p>This is an automated message from Airtel Inventory System.<br>\n");
        sb.append("            Please do not reply to this email.</p>\n");
        sb.append("            <p>&copy; 2024 Airtel Inventory Management System</p>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");
        sb.append("</div>\n");
        sb.append("</body>\n");
        sb.append("</html>");
        
        return sb.toString();
    }
    
    private String buildReturnEmailBody(Device device, Employee employee, AssignmentHistory assignment) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <style>\n");
        sb.append("        body { font-family: Arial, sans-serif; line-height: 1.6; }\n");
        sb.append("        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n");
        sb.append("        .header { background-color: #10b981; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }\n");
        sb.append("        .header h2 { color: white; margin: 0; }\n");
        sb.append("        .content { padding: 20px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 10px 10px; }\n");
        sb.append("        .device-details { background-color: #f3f4f6; padding: 15px; border-radius: 8px; margin: 15px 0; }\n");
        sb.append("        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #e5e7eb; font-size: 12px; color: #6b7280; text-align: center; }\n");
        sb.append("    </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("<div class=\"container\">\n");
        sb.append("    <div class=\"header\">\n");
        sb.append("        <h2>✅ Device Return Confirmation</h2>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"content\">\n");
        sb.append("        <p>Dear <strong>").append(employee.getName()).append("</strong>,</p>\n");
        sb.append("        <p>The following device has been successfully returned to inventory:</p>\n");
        sb.append("        <div class=\"device-details\">\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">🔖 Asset Tag:</span> <strong>").append(device.getAssetTag()).append("</strong>\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">💻 Device Type:</span> ").append(device.getDeviceType()).append("\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">🏭 Brand/Model:</span> ").append(device.getBrand() != null ? device.getBrand() : "").append(" ").append(device.getModel() != null ? device.getModel() : "").append("\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">📅 Return Date:</span> ").append(assignment.getActualReturnDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"))).append("\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">💚 Return Condition:</span> <strong>").append(assignment.getConditionAtReturn() != null ? assignment.getConditionAtReturn() : "GOOD").append("</strong>\n");
        sb.append("            </div>\n");
        sb.append("        </div>\n");
        sb.append("        <p>Thank you for returning the device. Your cooperation is appreciated.</p>\n");
        sb.append("        <div class=\"footer\">\n");
        sb.append("            <p>This is an automated message from Airtel Inventory System.</p>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");
        sb.append("</div>\n");
        sb.append("</body>\n");
        sb.append("</html>");
        
        return sb.toString();
    }
    
    private String buildOverdueEmailBody(Device device, Employee employee, AssignmentHistory assignment, long daysOverdue) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <style>\n");
        sb.append("        body { font-family: Arial, sans-serif; line-height: 1.6; }\n");
        sb.append("        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n");
        sb.append("        .header { background-color: #ef4444; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }\n");
        sb.append("        .header h2 { color: white; margin: 0; }\n");
        sb.append("        .content { padding: 20px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 10px 10px; }\n");
        sb.append("        .urgent { background-color: #fee2e2; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #ef4444; }\n");
        sb.append("        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #e5e7eb; font-size: 12px; color: #6b7280; text-align: center; }\n");
        sb.append("    </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("<div class=\"container\">\n");
        sb.append("    <div class=\"header\">\n");
        sb.append("        <h2>⚠️ URGENT: Device Overdue Alert</h2>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"content\">\n");
        sb.append("        <p>Dear <strong>").append(employee.getName()).append("</strong>,</p>\n");
        sb.append("        <div class=\"urgent\">\n");
        sb.append("            <p><strong>⚠️ This device is <span style=\"color: red; font-size: 18px;\">").append(daysOverdue).append(" days overdue</span>!</strong></p>\n");
        sb.append("        </div>\n");
        sb.append("        <p>The following device requires immediate return:</p>\n");
        sb.append("        <div class=\"device-details\">\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">🔖 Asset Tag:</span> <strong>").append(device.getAssetTag()).append("</strong>\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">💻 Device Type:</span> ").append(device.getDeviceType()).append("\n");
        sb.append("            </div>\n");
        sb.append("            <div class=\"detail-row\">\n");
        sb.append("                <span class=\"label\">📅 Expected Return Date:</span> <strong style=\"color: red;\">").append(assignment.getExpectedReturnDate() != null ? assignment.getExpectedReturnDate().toString() : "Not specified").append("</strong>\n");
        sb.append("            </div>\n");
        sb.append("        </div>\n");
        sb.append("        <p><strong>Action Required:</strong> Please return this device immediately to avoid further escalation.</p>\n");
        sb.append("        <p>If you have any questions or need an extension, please contact your IT administrator.</p>\n");
        sb.append("        <div class=\"footer\">\n");
        sb.append("            <p>This is an automated reminder from Airtel Inventory System.</p>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");
        sb.append("</div>\n");
        sb.append("</body>\n");
        sb.append("</html>");
        
        return sb.toString();
    }
    
    private String buildWelcomeEmailBody(Employee employee) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <style>\n");
        sb.append("        body { font-family: Arial, sans-serif; line-height: 1.6; }\n");
        sb.append("        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n");
        sb.append("        .header { background-color: #dc2626; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }\n");
        sb.append("        .header h2 { color: white; margin: 0; }\n");
        sb.append("        .content { padding: 20px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 10px 10px; }\n");
        sb.append("        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #e5e7eb; font-size: 12px; color: #6b7280; text-align: center; }\n");
        sb.append("    </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("<div class=\"container\">\n");
        sb.append("    <div class=\"header\">\n");
        sb.append("        <h2>🎉 Welcome to Airtel Inventory System</h2>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"content\">\n");
        sb.append("        <p>Dear <strong>").append(employee.getName()).append("</strong>,</p>\n");
        sb.append("        <p>Welcome to the Airtel Inventory Management System!</p>\n");
        sb.append("        <p>You have been registered in our system. You will receive email notifications when:</p>\n");
        sb.append("        <ul>\n");
        sb.append("            <li>📱 A device is assigned to you</li>\n");
        sb.append("            <li>✅ A device is returned successfully</li>\n");
        sb.append("            <li>⚠️ A device becomes overdue</li>\n");
        sb.append("        </ul>\n");
        sb.append("        <p>Please keep your contact information up to date to ensure you receive important notifications.</p>\n");
        sb.append("        <p>If you have any questions, please contact your IT administrator.</p>\n");
        sb.append("        <div class=\"footer\">\n");
        sb.append("            <p>This is an automated message from Airtel Inventory System.</p>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");
        sb.append("</div>\n");
        sb.append("</body>\n");
        sb.append("</html>");
        
        return sb.toString();
    }
    
    private String buildDeviceRegistrationEmailBody(Device device) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <style>\n");
        sb.append("        body { font-family: Arial, sans-serif; line-height: 1.6; }\n");
        sb.append("        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n");
        sb.append("        .header { background-color: #3b82f6; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }\n");
        sb.append("        .header h2 { color: white; margin: 0; }\n");
        sb.append("        .content { padding: 20px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 10px 10px; }\n");
        sb.append("        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #e5e7eb; font-size: 12px; color: #6b7280; text-align: center; }\n");
        sb.append("    </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("<div class=\"container\">\n");
        sb.append("    <div class=\"header\">\n");
        sb.append("        <h2>🆕 New Device Registered</h2>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"content\">\n");
        sb.append("        <p>A new device has been added to the inventory system:</p>\n");
        sb.append("        <div class=\"device-details\">\n");
        sb.append("            <div class=\"detail-row\">🔖 Asset Tag: <strong>").append(device.getAssetTag()).append("</strong></div>\n");
        sb.append("            <div class=\"detail-row\">📌 Serial Number: ").append(device.getSerialNumber()).append("</div>\n");
        sb.append("            <div class=\"detail-row\">💻 Type: ").append(device.getDeviceType()).append("</div>\n");
        sb.append("            <div class=\"detail-row\">🏭 Brand: ").append(device.getBrand() != null ? device.getBrand() : "N/A").append("</div>\n");
        sb.append("            <div class=\"detail-row\">🔧 Model: ").append(device.getModel() != null ? device.getModel() : "N/A").append("</div>\n");
        sb.append("        </div>\n");
        sb.append("        <div class=\"footer\">\n");
        sb.append("            <p>Admin notification from Airtel Inventory System.</p>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");
        sb.append("</div>\n");
        sb.append("</body>\n");
        sb.append("</html>");
        
        return sb.toString();
    }
}