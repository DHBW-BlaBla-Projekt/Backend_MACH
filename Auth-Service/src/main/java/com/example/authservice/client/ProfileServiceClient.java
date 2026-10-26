package com.example.authservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Client zum Synchronisieren von User-Daten mit dem Profile-Service.
 */
@Service
public class ProfileServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceClient.class);

    @Value("${app.profile-service.url:http://localhost:8084}")
    private String profileServiceUrl;

    private final RestTemplate restTemplate;

    public ProfileServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Synchronisiert einen User mit dem Profile-Service.
     */
    public void syncUser(String username, String password, String role) {
        String url = profileServiceUrl + "/api/auth/users/sync";
        try {
            Map<String, String> body = new java.util.HashMap<>();
            body.put("username", username.toLowerCase());
            body.put("password", password != null ? password : "");
            body.put("role", role != null ? role : "USER");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("User {} erfolgreich mit Profile-Service synchronisiert", username);
            } else {
                log.warn("Sync von User {} fehlgeschlagen – Status: {}", username, response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Fehler beim Sync von User {} zum Profile-Service: {}", username, e.getMessage());
        }
    }

    public String getProfileServiceUrl() {
        return profileServiceUrl;
    }
}
