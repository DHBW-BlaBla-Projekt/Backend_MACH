package com.example.rideofferservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class OrsRouteService {

    private final RestClient client;
    private final String apiKey;

    public OrsRouteService(
            RestClient.Builder restClientBuilder,
            @Value("${app.openrouteservice.api-key:}") String apiKey,
            @Value("${app.openrouteservice.base-url:https://api.openrouteservice.org}") String baseUrl) {
        this.client = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public RouteResult calculateRoute(String startLocation, String endLocation) {
        if (apiKey.isBlank()) {
            throw new RouteCalculationException("OpenRouteService ist nicht konfiguriert.");
        }

        Coordinate start = geocode(startLocation);
        Coordinate end = geocode(endLocation);
        JsonNode route;
        try {
            route = client.post()
                    .uri("/v2/directions/driving-car/geojson")
                    .header("Authorization", apiKey)
                    .body(new CoordinatesRequest(List.of(
                            List.of(start.longitude(), start.latitude()),
                            List.of(end.longitude(), end.latitude()))))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new RouteCalculationException("OpenRouteService konnte die Route nicht berechnen.");
        }

        JsonNode feature = firstFeature(route, "Route konnte nicht berechnet werden.");
        JsonNode summary = feature.path("properties").path("summary");
        double distanceKm = summary.path("distance").asDouble(-1) / 1000;
        double durationMinutes = summary.path("duration").asDouble(-1) / 60;
        if (distanceKm <= 0 || durationMinutes < 0) {
            throw new RouteCalculationException("OpenRouteService lieferte keine gültigen Routendaten.");
        }

        return new RouteResult(
                start,
                end,
                feature.path("geometry").toString(),
                distanceKm,
                durationMinutes);
    }

    private Coordinate geocode(String location) {
        JsonNode response;
        try {
            response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/geocode/search")
                            .queryParam("api_key", apiKey)
                            .queryParam("text", location)
                            .queryParam("size", 1)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new RouteCalculationException("OpenRouteService konnte den Ort nicht auflösen.");
        }
        JsonNode feature = firstFeature(response, "Ort konnte nicht gefunden werden: " + location);
        JsonNode coordinates = feature.path("geometry").path("coordinates");
        if (!coordinates.isArray() || coordinates.size() < 2) {
            throw new RouteCalculationException("OpenRouteService lieferte keine Koordinaten für: " + location);
        }
        return new Coordinate(coordinates.get(1).asDouble(), coordinates.get(0).asDouble());
    }

    private JsonNode firstFeature(JsonNode response, String errorMessage) {
        JsonNode features = response == null ? null : response.path("features");
        if (features == null || !features.isArray() || features.isEmpty()) {
            throw new RouteCalculationException(errorMessage);
        }
        return features.get(0);
    }

    public record RouteResult(
            Coordinate start,
            Coordinate end,
            String geometry,
            double distanceKm,
            double durationMinutes) {}

    public record Coordinate(double latitude, double longitude) {}

    private record CoordinatesRequest(List<List<Double>> coordinates) {}

    public static class RouteCalculationException extends RuntimeException {
        public RouteCalculationException(String message) {
            super(message);
        }
    }
}
