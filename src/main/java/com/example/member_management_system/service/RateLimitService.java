package com.example.member_management_system.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Resolve a bucket for a given IP address or identifier
     * Default: 5 requests per minute for authentication endpoints
     */
    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createNewBucket());
    }

    private Bucket createNewBucket() {
        // Allow 5 requests per minute
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillIntervally(5, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Check if the request is allowed for the given key
     *
     * @param key Unique identifier (e.g., IP address, user identifier)
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean tryConsume(String key) {
        Bucket bucket = resolveBucket(key);
        boolean consumed = bucket.tryConsume(1);

        if (!consumed) {
            log.warn("Rate limit exceeded for key: {}", key);
        }

        return consumed;
    }
}

