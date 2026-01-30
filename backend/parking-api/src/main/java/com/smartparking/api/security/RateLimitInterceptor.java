package com.smartparking.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.api.util.ResponseWriter;
import com.smartparking.common.api.Result;
import com.smartparking.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final int capacity;
    private final int refillPerSecond;
    private final ObjectMapper objectMapper;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitInterceptor(@Value("${security.ratelimit.capacity:50}") int capacity,
                                @Value("${security.ratelimit.refillPerSecond:50}") int refillPerSecond,
                                ObjectMapper objectMapper) {
        this.capacity = Math.max(1, capacity);
        this.refillPerSecond = Math.max(1, refillPerSecond);
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String key = clientKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(capacity, System.nanoTime()));
        if (!bucket.tryConsume(capacity, refillPerSecond)) {
            ResponseWriter.writeJson(response, objectMapper, 429, Result.fail(ErrorCode.TOO_MANY_REQUESTS.getCode(), "TOO_MANY_REQUESTS"));
            return false;
        }
        return true;
    }

    private static String clientKey(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            int idx = ip.indexOf(',');
            return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
        }
        return request.getRemoteAddr();
    }

    private static final class Bucket {
        private int tokens;
        private long lastRefillNanos;

        private Bucket(int tokens, long lastRefillNanos) {
            this.tokens = tokens;
            this.lastRefillNanos = lastRefillNanos;
        }

        private synchronized boolean tryConsume(int capacity, int refillPerSecond) {
            refill(capacity, refillPerSecond);
            if (tokens <= 0) {
                return false;
            }
            tokens -= 1;
            return true;
        }

        private void refill(int capacity, int refillPerSecond) {
            long now = System.nanoTime();
            long elapsedNanos = now - lastRefillNanos;
            if (elapsedNanos <= 0) {
                return;
            }
            long refillTokens = (elapsedNanos * refillPerSecond) / 1_000_000_000L;
            if (refillTokens <= 0) {
                return;
            }
            tokens = (int) Math.min(capacity, (long) tokens + refillTokens);
            lastRefillNanos = now;
        }
    }
}
