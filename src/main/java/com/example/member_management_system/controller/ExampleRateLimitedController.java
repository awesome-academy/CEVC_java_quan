package com.example.member_management_system.controller;

import com.example.member_management_system.config.RateLimited;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * EXAMPLE: Cách áp dụng @RateLimited cho các API endpoints khác
 * <p>
 * Ưu điểm:
 * - Không cần inject RateLimitService
 * - Không cần HttpServletRequest parameter
 * - Không cần viết code lặp lại để check rate limit
 * - Chỉ cần thêm annotation @RateLimited
 */
@RestController
@RequestMapping("/api/example")
public class ExampleRateLimitedController {

    /**
     * Example 1: Sử dụng default message
     * Message mặc định: "Too many requests. Please try again later."
     */
    @PostMapping("/action1")
    @RateLimited  // Chỉ cần thêm annotation này
    public ResponseEntity<String> action1() {
        return ResponseEntity.ok("Action 1 executed");
    }

    /**
     * Example 2: Sử dụng custom message
     */
    @PostMapping("/action2")
    @RateLimited(message = "Too many requests for action2. Please wait 1 minute.")
    public ResponseEntity<String> action2() {
        return ResponseEntity.ok("Action 2 executed");
    }

    /**
     * Example 3: Password reset endpoint
     */
    @PostMapping("/forgot-password")
    @RateLimited(message = "Too many password reset attempts. Please try again later.")
    public ResponseEntity<Void> forgotPassword(@RequestBody String email) {
        // Business logic here
        return ResponseEntity.ok().build();
    }

    /**
     * Example 4: Registration endpoint
     */
    @PostMapping("/register")
    @RateLimited(message = "Too many registration attempts. Please try again in 1 minute.")
    public ResponseEntity<String> register(@RequestBody String userData) {
        // Business logic here
        return ResponseEntity.ok("User registered");
    }

    /**
     * Example 5: Không rate limit
     * Nếu không có @RateLimited annotation, endpoint sẽ không bị rate limit
     */
    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This endpoint has no rate limit");
    }

    /**
     * Example 6: Rate limit cho sensitive operations
     */
    @DeleteMapping("/delete-account")
    @RateLimited(message = "Too many delete account attempts. This is a sensitive operation.")
    public ResponseEntity<Void> deleteAccount() {
        // Business logic here
        return ResponseEntity.ok().build();
    }

    /**
     * Example 7: Rate limit cho file upload
     */
    @PostMapping("/upload")
    @RateLimited(message = "Too many upload requests. Please wait before uploading again.")
    public ResponseEntity<String> uploadFile(@RequestParam("file") String file) {
        // Business logic here
        return ResponseEntity.ok("File uploaded");
    }
}

