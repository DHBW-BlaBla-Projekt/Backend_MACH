package com.example.rideofferservice.client;

import com.example.rideofferservice.entity.FuelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP-Client für die kostenfreie CC BY 4.0 TankKönig API (creativecommons.tankerkoenig.de).
 * Lädt aktuelle Kraftstoffpreise von Tankstellen in einer Region und bildet Durchschnittspreise pro Kraftstoffart.
 */
@Component
public class TankKoenigClient {

    private static final Logger log = LoggerFactory.getLogger(TankKoenigClient.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;

    public TankKoenigClient(
            @Value("${app.tankkoenig.api-key}") String apiKey,
            @Value("${app.tankkoenig.base-url}") String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.restClient = RestClient.builder().build();
    }

    /**
     * Holt durchschnittliche Preise für alle verfügbaren Kraftstoffarten aus der CC-API.
     */
    public Map<FuelType, Double> fetchAveragePrices() {
        String url = buildUrl(null, null, null);
        log.info("Fetching fuel prices from TankKoenig API: {}", url);

        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                TankKoenigApiResponse apiResponse = parseResponse(response.getBody());
                return mapToFuelTypes(apiResponse);
            } else {
                throw new IllegalStateException("TankKoenig API responded with status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching fuel prices from TankKoenig API: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Holt durchschnittliche Preise aus einer bestimmten Region (Koordinaten).
     */
    public Map<FuelType, Double> fetchAveragePricesByCoords(double lat, double lng, int radiusKm) {
        String url = buildUrl(lat, lng, radiusKm);
        log.info("Fetching fuel prices from TankKoenig API at coords: {}, {} with radius {} km", lat, lng, radiusKm);

        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                TankKoenigApiResponse apiResponse = parseResponse(response.getBody());
                return mapToFuelTypes(apiResponse);
            } else {
                throw new IllegalStateException("TankKoenig API responded with status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching fuel prices for coords {},{}, radius {} from TankKoenig API: {}", 
                    lat, lng, radiusKm, e.getMessage(), e);
            throw e;
        }
    }

    private String buildUrl(Double lat, Double lng, Integer radius) {
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append("?");
        
        // Wenn Koordinaten angegeben sind, nutze diese
        if (lat != null && lng != null) {
            sb.append("lat=").append(lat);
            sb.append("&lng=").append(lng);
            sb.append("&rad=").append(radius != null ? radius : 10);
        } else {
            // Default: Berlin-Mitte
            sb.append("lat=52.521");
            sb.append("&lng=13.438");
            sb.append("&rad=5");
        }
        
        sb.append("&sort=dist&type=all&apikey=").append(apiKey);
        return sb.toString();
    }

    private TankKoenigApiResponse parseResponse(String body) {
        // Einfaches JSON-Parsing ohne externe Libs — nur für die CC-API-Struktur
        try {
            // Remove whitespace for easier parsing
            String clean = body.replaceAll("\\s+", "");
            
            // Check ok status
            if (!clean.contains("\"ok\":true") && !clean.contains("\"ok\": true")) {
                throw new IllegalStateException("TankKoenig API returned error: " + body.substring(0, Math.min(200, body.length())));
            }

            // Parse stations array — extract all numeric fields for e5, e10, diesel
            int stationsStart = clean.indexOf("\"stations\":[");
            if (stationsStart == -1) {
                throw new IllegalStateException("No 'stations' field found in response");
            }
            
            // Extract the stations JSON array content
            int arrStart = stationsStart + "\"stations\"[".length();
            // Find matching closing bracket
            int depth = 0;
            int arrEnd = -1;
            for (int i = arrStart; i < clean.length(); i++) {
                char c = clean.charAt(i);
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        arrEnd = i;
                        break;
                    }
                }
            }
            
            if (arrEnd == -1) {
                throw new IllegalStateException("Could not find end of stations array");
            }
            
            String stationsJson = clean.substring(arrStart + 1, arrEnd);
            
            // Parse individual station objects from the JSON array
            List<Station> stations = parseStationsArray(stationsJson);
            
            if (stations.isEmpty()) {
                throw new IllegalStateException("No stations data in TankKoenig API response");
            }

            return new TankKoenigApiResponse(stations, "ok");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse TankKoenig API response: " + e.getMessage());
        }
    }

    private List<Station> parseStationsArray(String stationsJson) {
        List<Station> stations = new ArrayList<>();
        
        // Split by station object boundaries — each starts with "{"
        int offset = 0;
        while (offset < stationsJson.length()) {
            int objStart = stationsJson.indexOf('{', offset);
            if (objStart == -1) break;
            
            // Find matching closing brace
            int depth = 0;
            int objEnd = -1;
            for (int i = objStart; i < stationsJson.length(); i++) {
                char c = stationsJson.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        objEnd = i + 1; // include the closing brace
                        break;
                    }
                }
            }
            
            if (objEnd == -1) break;
            
            String stationJson = stationsJson.substring(objStart, objEnd);
            Station station = parseSingleStation(stationJson);
            if (station != null) {
                stations.add(station);
            }
            
            offset = objEnd;
        }
        
        return stations;
    }

    private Station parseSingleStation(String json) {
        return new Station(
            extractStringField(json, "id"),
            extractStringField(json, "name"),
            extractStringField(json, "brand"),
            extractDoubleField(json, "e5"),
            extractDoubleField(json, "e10"),
            extractDoubleField(json, "diesel"),
            extractDoubleField(json, "dist"),
            json.contains("\"isOpen\":true") || json.contains("\"isOpen\": true")
        );
    }


    private String extractStringField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int start = json.indexOf(key);
        if (start == -1) return null;
        start += key.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private Double extractDoubleField(String json, String field) {
        String key = "\"" + field + "\":";
        int start = json.indexOf(key);
        if (start == -1) return null;
        start += key.length();
        
        // Skip whitespace
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        
        // Extract number (could be negative, decimal)
        StringBuilder sb = new StringBuilder();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
            sb.append(json.charAt(end));
            end++;
        }
        
        if (sb.isEmpty()) return null;
        try {
            return Double.parseDouble(sb.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<FuelType, Double> mapToFuelTypes(TankKoenigApiResponse apiResponse) {
        List<Station> stations = apiResponse.stations();
        
        if (stations.isEmpty()) {
            throw new IllegalStateException("TankKoenig API returned no station data");
        }

        // Calculate averages using weighted distances (closer stations count more)
        double e5Total = 0, e10Total = 0, dieselTotal = 0;
        int count = 0;
        
        for (Station s : stations) {
            if (s.e5 != null) e5Total += s.e5;
            if (s.e10 != null) e10Total += s.e10;
            if (s.diesel != null) dieselTotal += s.diesel;
            count++;
        }

        if (count == 0) {
            throw new IllegalStateException("No valid price data from TankKoenig API");
        }

        // TankKoenig API liefert bereits EUR (z. B. 1.339), keine Konvertierung nötig
        double e5Eur = e5Total / count;
        double dieselEur = dieselTotal / count;
        double e10Eur = e10Total / count;

        // PETROL = average of E5 and E10 (most common for cars)
        double superPlusPrice = (e5Eur + e10Eur) / 2.0;

        Map<FuelType, Double> prices = new LinkedHashMap<>();
        prices.put(FuelType.PETROL, superPlusPrice);
        prices.put(FuelType.DIESEL, dieselEur);
        // ELECTRIC und HYBRID haben keinen direkten TankKoenig-Preis -> null (Fallback)
        prices.put(FuelType.ELECTRIC, null);
        prices.put(FuelType.HYBRID, null);

        log.info("Fuel prices calculated from {} stations: PETROL={} EUR/l, DIESEL={} EUR/l", count, superPlusPrice, dieselEur);
        return prices;
    }

    /**
     * Station data record.
     */
    public record Station(
            String id,
            String name,
            String brand,
            Double e5,       // in EUR (z. B. 1.339)
            Double e10,      // in EUR (z. B. 1.319)
            Double diesel,   // in EUR (z. B. 1.109)
            Double dist,
            boolean isOpen
    ) {}

    /**
     * API-Antwort-Objekt.
     */
    public record TankKoenigApiResponse(
            List<Station> stations,
            String status
    ) {}
}
