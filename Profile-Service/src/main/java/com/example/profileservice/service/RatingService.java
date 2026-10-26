package com.example.profileservice.service;

import com.example.profileservice.dto.RatingRequest;
import com.example.profileservice.dto.RatingResponse;
import com.example.profileservice.model.entity.Rating;
import com.example.profileservice.repository.RatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service für die Bewertungslogik.
 * Enthält: Validierung, Speicherung und Abfrage von Bewertungen.
 */
@Service
public class RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingService.class);

    private final RatingRepository ratingRepository;
    private final RestTemplate restTemplate;
    private final String bookingServiceUrl;
    private final boolean ratingCheckEnabled;

    public RatingService(RatingRepository ratingRepository,
                         @Value("${app.booking-service.url:http://localhost:8082}") String bookingServiceUrl,
                         @Value("${app.rating.check-enabled:true}") boolean checkEnabled) {
        this.ratingRepository = ratingRepository;
        this.restTemplate = new RestTemplate();
        this.bookingServiceUrl = bookingServiceUrl;
        this.ratingCheckEnabled = checkEnabled;
    }

    /**
     * Erstellt eine neue Bewertung eines Fahrers durch einen Passagier.
     */
    @Transactional
    public RatingResponse rate(RatingRequest request, String passengerUsername) {
        // 1. Validierung: Rating muss zwischen 1 und 5 liegen
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Bewertung muss zwischen 1 und 5 sein.");
        }

        // 2. Validierung: Bewertung darf nur einmal pro Pair existieren
        ratingRepository.findByPassengerUsernameAndDriverUsername(passengerUsername, request.getDriverUsername())
                .ifPresent(existing -> {
                    throw new IllegalStateException("Du hast diesen Fahrer bereits bewertet!");
                });

        // 3. Validierung: Buchung muss existieren und abgeschlossen sein (über Booking-Service prüfen)
        boolean validBooking = checkValidRatingBooking(
                request.getRideId(), passengerUsername);

        if (!validBooking) {
            throw new IllegalStateException("Du kannst nur Fahrer bewerten, mit denen du eine gültige Buchung hattest.");
        }

        // 4. Bewertung speichern
        Rating rating = new Rating(
                passengerUsername,
                request.getRideId(),
                request.getDriverUsername(),
                request.getRating(),
                request.getComment()
        );

        Rating saved = ratingRepository.save(rating);
        log.info("Neue Bewertung erstellt: {} → {} ({} Stern(e))", 
                passengerUsername, request.getDriverUsername(), request.getRating());
        return mapToResponse(saved);
    }

    /**
     * Prüft via HTTP beim Booking-Service, ob eine gültige Bewertung möglich ist.
     */
    private boolean checkValidRatingBooking(Long rideId, String passengerUsername) {
        if (!ratingCheckEnabled) {
            log.debug("Bewertungs-Check deaktiviert – Rating wird ohne Buchungseingabe gespeichert.");
            return true;
        }

        String url = bookingServiceUrl + "/api/bookings/validate-rating?rideId=" 
                + rideId + "&passengerUsername=" + passengerUsername;

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object validObj = response.getBody().get("valid");
                boolean isValid = validObj instanceof Boolean ? (Boolean) validObj : false;
                log.debug("Booking-Validierung für rideId={} erfolgreich: {}", rideId, isValid);
                return isValid;
            }
            log.warn("Booking-Validierung gab unerwarteten Status zurück: {}", response.getStatusCode());
            return false;

        } catch (ResourceAccessException e) {
            log.error("Booking-Service ({}) ist nicht erreichbar. Rating wird trotzdem gespeichert.", 
                    bookingServiceUrl, e);
            // Fallback: wenn Booking-Service offline, erlauben wir die Bewertung
            // In Produktion: Monitoring + Alerting wäre hier besser
            return true;
        } catch (Exception e) {
            log.error("Fehler bei der Booking-Validierung für rideId={}: {}", rideId, e.getMessage());
            return false;
        }
    }

    /**
     * Ruft alle Bewertungen eines Fahrers ab.
     */
    public List<RatingResponse> getDriverRatings(String driverUsername) {
        List<Rating> ratings = ratingRepository.findByDriverUsername(driverUsername);
        return ratings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Berechnet den Durchschnittswert der Bewertungen eines Fahrers.
     */
    public double getAverageRating(String driverUsername) {
        List<Rating> ratings = ratingRepository.findByDriverUsername(driverUsername);
        if (ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);
    }

    private RatingResponse mapToResponse(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getPassengerUsername(),
                rating.getRideId(),
                rating.getDriverUsername(),
                rating.getRating(),
                rating.getComment(),
                rating.getCreatedAt()
        );
    }
}
