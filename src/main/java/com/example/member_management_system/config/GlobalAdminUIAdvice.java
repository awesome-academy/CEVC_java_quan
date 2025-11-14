package com.example.member_management_system.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalAdminUIAdvice {
    /**
     * Automatically add 'activePage' to the Model for every request.
     * This logic solves the problem of Spring Boot 3 (no #request in Thymeleaf)
     * by handling the logic on the Java side.
     *
     * @param request HttpServletRequest
     * @return Name of the active page (e.g., "dashboard", "positions")
     */
    @ModelAttribute("activePage")
    public String addActivePage(HttpServletRequest request) {
        String servletPath = request.getServletPath();

        if (servletPath.startsWith("/admin/positions")) {
            return "positions";
        }
        if (servletPath.startsWith("/admin/dashboard")) {
            return "dashboard";
        }

        return "";
    }
}
