package com.example.bookingservice.service;

import com.example.bookingservice.dto.RideInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class RideOfferClient {

    private static final Logger log = LoggerFactory.getLogger(RideOfferClient.class);

    @Value("${app.ride-offer-service.url:http://localhost:8081}")
    private String rideOfferServiceUrl;

    private final RestTemplate restTemplate;

    public RideOfferClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Hole eine einzelne Mitfahrt über den Ride-Offer-Service.
     */
    public RideInfoDto getRideById(Long rideId) {
        String url = rideOfferServiceUrl + "/api/rides/" + rideId;
        try {
            ResponseEntity<RideInfoDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, RideInfoDto.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.error("Fehler beim Abruf der Mitfahrt {}: {}", rideId, e.getMessage());
            throw new RuntimeException("Kann Ride-Offer-Service nicht erreichen: " + e.getMessage());
        }
    }

    /**
     * Dekrementiere die verfügbaren Sitze um 1 für eine gegebene Mitfahrt.
     */
    public void decrementAvailableSeats(Long rideId) {
        String url = rideOfferServiceUrl + "/api/rides/" + rideId + "/decrement";
        try {
            restTemplate.exchange(url, HttpMethod.PATCH, null, Void.class);
            log.info("Verfügbare Plätze für Mitfahrt {} dekrementiert.", rideId);
        } catch (Exception e) {
            log.error("Fehler beim Dekrementieren der verfügbaren Plätze für Ride {}: {}", rideId, e.getMessage());
        }
    }

    /**
     * Inkrementiere die verfügbaren Sitze um 1 für eine gegebene Mitfahrt (wird bei Stornierung genutzt).
     */
    public void incrementAvailableSeats(Long rideId) {
        String url = rideOfferServiceUrl + "/api/rides/" + rideId + "/increment";
        try {
            restTemplate.exchange(url, HttpMethod.PATCH, null, Void.class);
            log.info("Verfügbare Plätze für Mitfahrt {} inkrementiert.", rideId);
        } catch (Exception e) {
            log.error("Fehler beim Inkrementieren der verfügbaren Plätze für Ride {}: {}", rideId, e.getMessage());
        }
    }

    /**
     * Storniere eine Buchung im Ride-Offer-Service (optional).
     */
    public void notifyRideCancelled(Long rideId) {
        String url = rideOfferServiceUrl + "/api/rides/" + rideId + "/cancel";
        try {
            restTemplate.exchange(url, HttpMethod.PATCH, null, Void.class);
            log.info("Ride {} storniert über Ride-Offer-Service", rideId);
        } catch (Exception e) {
            log.warn("Fehler beim Stornieren der Mitfahrt {}: {}", rideId, e.getMessage());
        }
    }

    /**
     * Hole den URL des Ride-Offer-Services.
     */
    public String getRideOfferServiceUrl() {
        return rideOfferServiceUrl;
    }
}
