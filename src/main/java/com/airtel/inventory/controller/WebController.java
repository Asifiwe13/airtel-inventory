package com.airtel.inventory.controller;

import com.airtel.inventory.service.DeviceService;
import com.airtel.inventory.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Arrays;
import java.util.List;

@Controller
public class WebController {
    
    private final DeviceService deviceService;
    private final EmployeeService employeeService;
    
    public WebController(DeviceService deviceService, EmployeeService employeeService) {
        this.deviceService = deviceService;
        this.employeeService = employeeService;
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Use Object type instead of explicit DeviceStatistics
        model.addAttribute("stats", deviceService.getStatistics());
        model.addAttribute("recentDevices", deviceService.getAllDevices().stream().limit(5).toList());
        model.addAttribute("highRiskDevices", deviceService.getHighRiskDevices());
        return "dashboard";
    }
    
    @GetMapping("/devices")
    public String devices(Model model) {
        model.addAttribute("devices", deviceService.getAllDevices());
        return "devices";
    }
    
    @GetMapping("/devices/register")
    public String showRegisterForm(Model model) {
        List<String> deviceTypes = Arrays.asList("LAPTOP", "DESKTOP", "MOBILE", "TABLET", "NETWORK_EQUIPMENT");
        model.addAttribute("deviceTypes", deviceTypes);
        return "register-device";
    }
    
    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "employees";
    }
    
    @GetMapping("/employees/register")
    public String showEmployeeRegisterForm() {
        return "register-employee";
    }
    
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("stats", deviceService.getStatistics());
        return "reports";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/assignments")
    public String assignments(Model model) {
        return "assignments";
    }
    
    @GetMapping("/scanner")
    public String scanner(Model model) {
        return "scanner";
    }
    
    @GetMapping("/track")
    public String trackDevice() {
        return "track";
    }	
    
    @GetMapping("/ai-test")
    public String aiTest() {
        return "ai-test";
    }
}