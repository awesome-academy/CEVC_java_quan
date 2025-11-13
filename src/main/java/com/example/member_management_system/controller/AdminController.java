package com.example.member_management_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String showDashboard() {
        return "admin/dashboard";
    }

    /**
     * Show login page with error and logout messages
     *
     * @param error  Parameter passed from SecurityConfig if login fails
     * @param logout Parameter passed from SecurityConfig on logout
     */
    @GetMapping("/login")
    public String showLoginPage(Model model,
                                @RequestParam(value = "error", required = false) Boolean error,
                                @RequestParam(value = "logout", required = false) Boolean logout) {

        if (Boolean.TRUE.equals(error)) {
            model.addAttribute("errorMessage", "Email or password is incorrect.");
        }
        if (Boolean.TRUE.equals(logout)) {
            model.addAttribute("logoutMessage", "Logout successful.");
        }

        return "admin/login";
    }
}
