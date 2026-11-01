package com.example.authservice.ratelimit;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Einfacher In-Memory Rate Limiter für Auth-Endpunkte.
 * Schützt /register und /login vor Brute-Force über IP oder Username.
 *
 * Limit: MAX_ATTEMPTS Versuche pro Window (WINDOW_MILLIS).
 * Nach erfolgreichem Login/Registration wird der Zähler zurückgesetzt.
 * Wenn das Limit erreicht ist, werden weitere Versuche mit HTTP 429 abgewiesen.
 */
@Component
public class RateLimiter {

    public interface Config {
        int MAX_ATTEMPTS = 5;
        long WINDOW_MILLIS = 60_000L; // 1 Minute
    }

    private final ConcurrentHashMap<String, AttemptInfo> attemptsByIp = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptInfo> attemptsByUsername = new ConcurrentHashMap<>();

    public boolean tryConsume(String key, ConcurrentHashMap<String, AttemptInfo> storage) {
        long now = System.currentTimeMillis();


        AttemptInfo info = storage.compute(key, (k, existing) -> {
            if (existing == null || (now - existing.startTime) > Config.WINDOW_MILLIS) {
                AttemptInfo newInfo = new AttemptInfo(now);
                return newInfo;
            }

            existing.attempts.incrementAndGet();
            return existing;
        });

        if (info.attempts.get() > Config.MAX_ATTEMPTS) {
           
            return false;
        }

        return true;
    }


    public long getRemainingAttempts(String key, ConcurrentHashMap<String, AttemptInfo> storage) {
        AttemptInfo info = storage.get(key);
        if (info == null) return Config.MAX_ATTEMPTS;

        long now = System.currentTimeMillis();
        if ((now - info.startTime) > Config.WINDOW_MILLIS) return Config.MAX_ATTEMPTS;

        int remaining = Config.MAX_ATTEMPTS - info.attempts.get();
        return Math.max(0, remaining);
    }


    public void recordSuccess(String key, ConcurrentHashMap<String, AttemptInfo> storage) {
        AttemptInfo info = storage.get(key);
        if (info != null) {
            long now = System.currentTimeMillis();
            if ((now - info.startTime) <= Config.WINDOW_MILLIS) {
                info.attempts.set(0);
                info.successAt = now;
            }
        }
    }


    public ConcurrentHashMap<String, AttemptInfo> getAttemptsByIpMap() {
        return attemptsByIp;
    }

    public ConcurrentHashMap<String, AttemptInfo> getAttemptsByUsernameMap() {
        return attemptsByUsername;
    }

    private static class AttemptInfo {
        final long startTime;
        AtomicInteger attempts;
        volatile long successAt = 0;

        AttemptInfo(long startTime) {
            this.startTime = startTime;
            this.attempts = new AtomicInteger(1);
        }
    }
}
