package com.example.authservice.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring-Filter, der /api/auth/login und /api/auth/register vor Brute-Force schützt.
 * Prüft ausschließlich das IP-basierte Limit im Filter (immer aktiv).
 * Das Username-Limit wird im Controller geprüft (braucht den Body).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/auth/login") && !path.startsWith("/api/auth/register");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = getClientIp(request);

        if (!rateLimiter.tryConsume(ip, rateLimiter.getAttemptsByIpMap())) {
            writeRateLimitResponse(response, RateLimitResponse.ofIp());
            return;
        }


        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeRateLimitResponse(HttpServletResponse response, RateLimitResponse rateLimitResponse) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), rateLimitResponse);
    }
}
