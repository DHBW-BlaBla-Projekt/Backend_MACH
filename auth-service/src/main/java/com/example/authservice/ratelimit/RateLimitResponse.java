package com.example.authservice.ratelimit;

/**
 * Standardisierte Rate-Limit Fehlerantwort.
 */
public record RateLimitResponse(
        int statusCode,
        String error
) {
    public static RateLimitResponse ofIp() {
        return new RateLimitResponse(429, "Zu viele Anfragen von dieser IP. Bitte versuche es in 60 Sekunden erneut.");
    }

    public static RateLimitResponse ofUsername(String username) {
        return new RateLimitResponse(429, "Zu viele Login-Versuche für '" + username + "'. Bitte versuche es in 60 Sekunden erneut.");
    }
}
