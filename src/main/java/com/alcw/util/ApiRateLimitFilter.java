package com.alcw.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> RATE_LIMITED_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify-otp",
            "/api/auth/resend-otp",
            "/api/auth/request-password-reset",
            "/api/contact"
    );

    private final Map<String, RequestWindow> requestTracker = new ConcurrentHashMap<>();

    @Value("${app.security.rate-limit.max-requests-per-minute:30}")
    private int maxRequestsPerMinute;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!isRateLimitedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = getClientKey(request);
        RequestWindow window = requestTracker.computeIfAbsent(key, ignored -> new RequestWindow());

        if (!window.tryConsume(maxRequestsPerMinute)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many requests. Please retry later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitedPath(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        return RATE_LIMITED_PATHS.stream().anyMatch(path -> request.getRequestURI().startsWith(path));
    }

    private String getClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RequestWindow {
        private Instant windowStart = Instant.now();
        private int counter = 0;

        synchronized boolean tryConsume(int maxRequestsPerMinute) {
            Instant now = Instant.now();
            if (Duration.between(windowStart, now).toSeconds() >= 60) {
                windowStart = now;
                counter = 0;
            }
            counter++;
            return counter <= maxRequestsPerMinute;
        }
    }
}
