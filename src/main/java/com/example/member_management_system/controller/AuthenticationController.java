package com.example.member_management_system.controller;

import com.example.member_management_system.config.RateLimited;
import com.example.member_management_system.dto.auth.AuthRequest;
import com.example.member_management_system.dto.auth.AuthResponse;
import com.example.member_management_system.dto.auth.RefreshTokenRequest;
import com.example.member_management_system.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for Login/Logout")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Returns JWT Access Token & Refresh Token")
    @RateLimited(message = "Too many login attempts. Please try again later.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Get new Access Token using Refresh Token")
    @RateLimited(message = "Too many refresh token requests. Please try again later.")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Client should discard the token. No server-side logout or logging is performed.")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}
