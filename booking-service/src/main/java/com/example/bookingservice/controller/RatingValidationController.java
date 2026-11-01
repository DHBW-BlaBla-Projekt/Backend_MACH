package com.example.bookingservice.controller;

import com.example.bookingservice.service.RatingValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller zur Validierung von Bewertungen.
 * 
 * <p>Dieser Endpunkt wird vom Auth-Service aufgerufen, um zu prüfen, ob eine Bewertung erlaubt ist.</p>
 */
@RestController
@RequestMapping("/api/bookings")
public class RatingValidationController {

    private final RatingValidationService ratingValidationService;

    public RatingValidationController(RatingValidationService ratingValidationService) {
        this.ratingValidationService = ratingValidationService;
    }


    @GetMapping("/validate-rating")
    public ResponseEntity<Map<String, Object>> validateRating(
            @RequestParam Long rideId,
            @RequestParam String passengerUsername) {
        
        boolean valid = ratingValidationService.isValidRating(rideId, passengerUsername);

        return ResponseEntity.ok(Map.of(
                "valid", valid,
                "rideId", rideId,
                "passengerUsername", passengerUsername
        ));
    }
}
