package com.example.member_management_system.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtils {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_EXPIRATION_MS}")
    private long jwtExpirationMs;

    @Value("${JWT_REFRESH_EXPIRATION_MS}")
    private long refreshExpirationMs;

    @PostConstruct
    public void validateConfiguration() {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("JWT_SECRET environment variable is not configured");
        }

        if (jwtExpirationMs <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION_MS must be greater than 0");
        }

        if (refreshExpirationMs <= 0) {
            throw new IllegalStateException("JWT_REFRESH_EXPIRATION_MS must be greater than 0");
        }

        // Validate that the secret is at least 256 bits (32 bytes) for HS256
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            if (keyBytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET must be at least 256 bits (32 bytes) when Base64 decoded for HS256 algorithm");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT_SECRET is not a valid Base64 encoded string", e);
        }

        log.info("JWT configuration validated successfully");
    }

    public String generateToken(UserDetails userDetails) {
        return buildToken(userDetails, jwtExpirationMs);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails, refreshExpirationMs);
    }

    private String buildToken(UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username;

        try {
            username = extractUsername(token);
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());

            return false;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());

            return false;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());

            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());

            return false;
        }
        // Now check expiration separately
        if (!username.equals(userDetails.getUsername())) {
            log.error("JWT token username does not match user details.");

            return false;
        }

        try {
            if (isTokenExpired(token)) {
                log.error("JWT token is expired.");

                return false;
            }
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());

            return false;
        }

        return true;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
