package com.example.bookingservice.service;

import com.example.bookingservice.dto.RideInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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
     * Storniere eine Buchung im Ride-Offer-Service (optional).
     */
    public void notifyRideCancelled(Long rideId) {
        String url = rideOfferServiceUrl + "/api/rides/" + rideId + "/cancel";
        try {
            restTemplate.patchForObject(url, null, Map.class);
            log.info("Ride {} storniert über Ride-Offer-Service", rideId);
        } catch (Exception e) {
            log.warn("Fehler beim Stornieren der Mitfahrt {}: {}", rideId, e.getMessage());
        }
    }
}
