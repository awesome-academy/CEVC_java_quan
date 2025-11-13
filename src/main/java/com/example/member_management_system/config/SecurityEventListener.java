package com.example.member_management_system.config;

import com.example.member_management_system.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityEventListener {

    private final ActivityLogService logService;

    @EventListener
    public void onLoginSuccess(AuthenticationSuccessEvent event) {
        UserDetails userDetails = (UserDetails) event.getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        logService.logActivity(email, "LOGIN", "Login successful.");
    }

    @EventListener
    public void onLoginFailure(AbstractAuthenticationFailureEvent event) {
        String email = event.getAuthentication().getName();
        String message = event.getException().getMessage();
        logService.logActivity(email, "LOGIN", "Login failed: " + message);
    }

    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        if (event.getAuthentication() != null && event.getAuthentication().getPrincipal() instanceof UserDetails userDetails) {
            String email = userDetails.getUsername();
            logService.logActivity(email, "LOGOUT", "Logout successful.");
        }
    }
}
