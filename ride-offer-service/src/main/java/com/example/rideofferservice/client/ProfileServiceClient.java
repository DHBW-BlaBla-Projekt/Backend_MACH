package com.example.rideofferservice.client;

import com.example.rideofferservice.dto.CarInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Client zum Abrufen von Car-Daten (Consumption, FuelType) aus dem Profile-Service.
 */
@Service
public class ProfileServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceClient.class);

    @Value("${app.profile-service.url:http://localhost:8082}")
    private String profileServiceUrl;

    private final RestTemplate restTemplate;

    public ProfileServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Holt die Car-Daten über die carId vom Profile-Service.
     */
    public CarInfoDto getCarById(Long carId) {
        String url = profileServiceUrl + "/api/profile/cars/" + carId;
        try {
            ResponseEntity<CarInfoDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, CarInfoDto.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Car {} nicht gefunden im Profile-Service", carId);
            return null;
        } catch (Exception e) {
            log.error("Fehler beim Abruf des Cars {}: {}", carId, e.getMessage());
            throw new RuntimeException("Kann Profile-Service nicht erreichen: " + e.getMessage());
        }
    }

    public String getProfileServiceUrl() {
        return profileServiceUrl;
    }
}
