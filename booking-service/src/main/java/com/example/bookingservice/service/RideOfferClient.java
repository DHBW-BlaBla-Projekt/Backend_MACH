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

    public RideOfferClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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

    public boolean tryBookSeat(Long rideId) {
        String url = rideOfferServiceUrl + "/api/rides/" + rideId + "/book";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            log.info("Sende PATCH request an: {}", url);
            
            ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException.Conflict e) {
            log.warn("Keine freien Plätze für Ride {}: {}", rideId, e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Fehler beim Buchen der Mitfahrt {}: {}", rideId, e.getMessage());
            throw new RuntimeException("Kann Ride-Offer-Service nicht erreichen: " + e.getMessage());
        }
    }

    public void decrementAvailableSeats(Long rideId) {
        String url = rideOfferServiceUrl + "/api/rides/" + rideId + "/decrement";
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
            log.info("Verfügbare Plätze für Mitfahrt {} dekrementiert.", rideId);
        } catch (Exception e) {
            log.error("Fehler beim Dekrementieren der verfügbaren Plätze für Ride {}: {}", rideId, e.getMessage());
        }
    }

    public void incrementAvailableSeats(Long rideId) {
        String url = rideOfferServiceUrl + "/api/rides/" + rideId + "/increment";
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
            log.info("Verfügbare Plätze für Mitfahrt {} inkrementiert.", rideId);
        } catch (Exception e) {
            log.error("Fehler beim Inkrementieren der verfügbaren Plätze für Ride {}: {}", rideId, e.getMessage());
            throw new RuntimeException("Sitzplatz konnte nicht freigegeben werden: " + e.getMessage(), e);
        }
    }

    public void notifyRideCancelled(Long rideId) {
        String url = rideOfferServiceUrl + "/api/rides/" + rideId + "/cancel";
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
            log.info("Ride {} storniert über Ride-Offer-Service", rideId);
        } catch (Exception e) {
            log.warn("Fehler beim Stornieren der Mitfahrt {}: {}", rideId, e.getMessage());
        }
    }

    public String getRideOfferServiceUrl() {
        return rideOfferServiceUrl;
    }
}
