package com.teklif.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${security.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${security.rate-limit.requests:100}")
    private int maxRequests;

    @Value("${security.rate-limit.window-seconds:60}")
    private int windowSeconds;

    // In-memory store for rate limiting (use Redis for distributed systems)
    private final ConcurrentHashMap<String, RateLimitEntry> rateLimitMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip rate limiting for GET requests on public endpoints
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (isPublicEndpoint(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = getClientId(request);
        String key = clientId + ":" + path;

        if (isRateLimited(key)) {
            log.warn("Rate limit exceeded for client: {} on path: {}", clientId, path);
            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path, String method) {
        return (path.startsWith("/api/auth/") && "POST".equals(method)) ||
               (path.startsWith("/api/offers/public/") && "GET".equals(method)) ||
               (path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/"));
    }

    private String getClientId(HttpServletRequest request) {
        // Use IP address as client identifier
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isRateLimited(String key) {
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (windowSeconds * 1000L);

        rateLimitMap.entrySet().removeIf(entry -> entry.getValue().getTimestamp() < windowStart);

        RateLimitEntry entry = rateLimitMap.computeIfAbsent(key,
                k -> new RateLimitEntry(currentTime, new AtomicInteger(0)));

        return entry.incrementAndGet() > maxRequests;
    }

    private static class RateLimitEntry {
        private final long timestamp;
        private final AtomicInteger requestCount;

        public RateLimitEntry(long timestamp, AtomicInteger requestCount) {
            this.timestamp = timestamp;
            this.requestCount = requestCount;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public int incrementAndGet() {
            return requestCount.incrementAndGet();
        }
    }
}
