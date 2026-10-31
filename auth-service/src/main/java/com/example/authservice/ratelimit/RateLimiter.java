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

    /**
     * Konfigurierbare Limits.
     */
    public interface Config {
        int MAX_ATTEMPTS = 5;
        long WINDOW_MILLIS = 60_000L; // 1 Minute
    }

    private final ConcurrentHashMap<String, AttemptInfo> attemptsByIp = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptInfo> attemptsByUsername = new ConcurrentHashMap<>();

    /**
     * Prüft ob ein Versuch erlaubt ist UND verbraucht gleichzeitig einen Slot.
     * Das ist eine atomare Operation – entweder alles oder nichts.
     * @return true wenn noch Slots verfügbar sind (und einer verbraucht wurde), false wenn das Limit erreicht ist.
     */
    public boolean tryConsume(String key, ConcurrentHashMap<String, AttemptInfo> storage) {
        long now = System.currentTimeMillis();


        AttemptInfo info = storage.compute(key, (k, existing) -> {
            if (existing == null || (now - existing.startTime) > Config.WINDOW_MILLIS) {
                // Neues Fenster starten – Counter auf 1 setzen
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

    /**
     * Gibt die verbleibenden Versuche für den gegebenen Key zurück.
     */
    public long getRemainingAttempts(String key, ConcurrentHashMap<String, AttemptInfo> storage) {
        AttemptInfo info = storage.get(key);
        if (info == null) return Config.MAX_ATTEMPTS;

        long now = System.currentTimeMillis();
        if ((now - info.startTime) > Config.WINDOW_MILLIS) return Config.MAX_ATTEMPTS;

        int remaining = Config.MAX_ATTEMPTS - info.attempts.get();
        return Math.max(0, remaining);
    }

    /**
     * Setzt den Zähler für diesen Key zurück (nach erfolgreichem Login/Registration).
     */
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

    /**
     * Gibt die Map für IP-basierte Attempts zurück (für Filter-Zugriff).
     */
    public ConcurrentHashMap<String, AttemptInfo> getAttemptsByIpMap() {
        return attemptsByIp;
    }

    /**
     * Gibt die Map für Username-basierte Attempts zurück (für Controller-Zugriff).
     */
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
