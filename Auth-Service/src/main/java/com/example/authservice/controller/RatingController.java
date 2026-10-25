package com.example.authservice.controller;

import com.example.authservice.dto.RatingRequest;
import com.example.authservice.dto.RatingResponse;
import com.example.authservice.model.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST-Controller für Bewertungs-Endpunkte.
 * 
 * <p>Dient der Bewertung von Fahrern durch Passagiere nach abgeschlossenen Fahrten.</p>
 * 
 * <h2>Verwendete Endpunkte:</h2>
 * <ul>
 *   <li>{@code POST /api/auth/rate} – Bewertet einen Fahrer (nur für aktive Passagier-Buchungen)</li>
 *   <li>{@code GET /api/auth/users/{username}/ratings} – Ruft alle Bewertungen eines Fahrers ab</li>
 *   <li>{@code GET /api/auth/users/{username}/average-rating} – Berechnet den Durchschnittswert der Bewertungen</li>
 * </ul>
 * 
 * <p>Diese Endpunkte erfordern eine gültige JWT-Authentifizierung.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class RatingController {

    private final RatingService ratingService;
    private final UserRepository userRepository;

    /**
     * Konstruktor mit Dependency Injection.
     */
    public RatingController(RatingService ratingService, UserRepository userRepository) {
        this.ratingService = ratingService;
        this.userRepository = userRepository;
    }

    /**
     * Bewertet einen Fahrer durch den authentifizierten Passagier.
     * 
     * <p>Voraussetzungen:</p>
     * <ul>
     *   <li>Passagier muss eine gültige, abgeschlossene Buchung für die Fahrt haben</li>
     *   <li>Der Fahrer darf nicht bereits von diesem Passagier bewertet worden sein</li>
     *   <li>Rating muss zwischen 1 und 5 liegen</li>
     * </ul>
     * 
     * @param request DTO mit Bewertungsdetails (rideId, driverUsername, rating, comment)
     * @return ResponseEntity mit der gespeicherten Bewertung oder Fehlermeldung
     */
    @PostMapping("/rate")
    public ResponseEntity<?> rate(@RequestBody RatingRequest request) {
        // Automatisch den aktuell authentifizierten Benutzer aus dem JWT-Token extrahieren
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                    "statusCode", 401,
                    "error", "Nicht authentifiziert"
            ));
        }
        
        String username = authentication.getName(); // Username aus dem JWT-Token

        // Sicherheitscheck: Der Benutzer muss existieren und aktiv sein
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of(
                    "statusCode", 401,
                    "error", "Benutzer nicht gefunden"
            ));
        }

        try {
            RatingResponse response = ratingService.rate(request, username);
            return ResponseEntity.ok(Map.of(
                    "statusCode", 201,
                    "message", "Bewertung erfolgreich!",
                    "rating", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "statusCode", 400,
                    "error", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of(
                    "statusCode", 409,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "statusCode", 500,
                    "error", "Interner Fehler bei der Bewertung: " + e.getMessage()
            ));
        }
    }

    /**
     * Ruft alle Bewertungen eines Fahrers ab.
     * 
     * @param username Username des bewerteten Fahrers
     * @return ResponseEntity mit einer Liste von Bewertungen
     */
    @GetMapping("/users/{username}/ratings")
    public ResponseEntity<?> getDriverRatings(@PathVariable String username) {
        List<RatingResponse> ratings = ratingService.getDriverRatings(username);
        return ResponseEntity.ok(Map.of(
                "statusCode", 200,
                "message", "Bewertungen erfolgreich abgerufen!",
                "ratings", ratings,
                "count", ratings.size()
        ));
    }

    /**
     * Berechnet den Durchschnittswert der Bewertungen eines Fahrers.
     * 
     * @param username Username des bewerteten Fahrers
     * @return ResponseEntity mit dem Durchschnittsrating
     */
    @GetMapping("/users/{username}/average-rating")
    public ResponseEntity<?> getAverageRating(@PathVariable String username) {
        double average = ratingService.getAverageRating(username);
        return ResponseEntity.ok(Map.of(
                "statusCode", 200,
                "message", "Durchschnittswert erfolgreich berechnet!",
                "username", username,
                "averageRating", Math.round(average * 100.0) / 100.0
        ));
    }
}
