package com.example.member_management_system.service;

import com.example.member_management_system.dto.auth.AuthRequest;
import com.example.member_management_system.dto.auth.AuthResponse;
import com.example.member_management_system.dto.auth.RefreshTokenRequest;
import com.example.member_management_system.repository.MemberRepository;
import com.example.member_management_system.util.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final MemberRepository memberRepository;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final ActivityLogService activityLogService;
    @Value("${JWT_EXPIRATION_MS}")
    private long jwtExpirationMs;

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = memberRepository.findByEmailWithRoles(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var jwtToken = jwtUtils.generateToken(user);
        var refreshToken = jwtUtils.generateRefreshToken(user);

        activityLogService.logActivity(user.getEmail(), "LOGIN", "Client API Login Success", "members", user.getId());

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtExpirationMs)
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        String userEmail;

        try {
            userEmail = jwtUtils.extractUsername(requestRefreshToken);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("Refresh token has expired");
        } catch (MalformedJwtException e) {
            throw new IllegalArgumentException("Malformed refresh token");
        } catch (UnsupportedJwtException e) {
            throw new IllegalArgumentException("Unsupported refresh token format");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Refresh token claims string is empty or invalid");
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid refresh token: " + e.getMessage());
        }

        if (userEmail != null) {
            var user = memberRepository.findByEmailWithRoles(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (jwtUtils.validateToken(requestRefreshToken, user)) {
                String newAccessToken = jwtUtils.generateToken(user);

                return AuthResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(requestRefreshToken)
                        .expiresIn(jwtExpirationMs)
                        .build();
            }
        }

        throw new IllegalArgumentException("Refresh token is invalid or expired");
    }
}
