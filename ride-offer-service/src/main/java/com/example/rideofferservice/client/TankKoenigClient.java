package com.example.rideofferservice.client;

import com.example.rideofferservice.entity.FuelType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * HTTP-Client für die TankKoenig API.
 * Ruft aktuelle durchschnittliche Kraftstoffpreise ab.
 */
@Component
public class TankKoenigClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;
//PLatzhalter, bis API Antrag durch ist.
    public TankKoenigClient(
            @Value("${app.tankkoenig.api-key}") String apiKey,
            @Value("${app.tankkoenig.base-url}") String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Ocp-Apim-Subscription-Key", apiKey)
                .build();
    }

    /**
     * Holt den durchschnittlichen Preis pro Liter für eine Kraftstoffart.
     */
    public Map<FuelType, Double> fetchAveragePrices() {
        var response = restClient.get()
                .uri("/fuel-prices/v2/average-fuel-prices")
                .retrieve()
                .body(TankKoenigApiResponse.class);

        if (response == null || response.data() == null) {
            throw new RuntimeException("TankKoenig API returned no data");
        }

        return mapToFuelTypes(response.data());
    }

    private Map<FuelType, Double> mapToFuelTypes(TankKoenigApiResponse.FuelPricesData prices) {
        double e5Price = extractPrice(prices.e5());
        double dieselPrice = extractPrice(prices.diesel());
        Double superPlusPrice = extractPriceNullable(prices.superPlus());

        return Map.of(
                FuelType.PETROL, averagePetrolPrice(e5Price, superPlusPrice),
                FuelType.DIESEL, dieselPrice,
                // ELECTRIC und HYBRID haben keinen direkten TankKoenig-Preis -> null (Fallback)
                FuelType.ELECTRIC, null,
                FuelType.HYBRID, null
        );
    }

    private double extractPrice(Double price) {
        return price != null ? price / 100.0 : throwPriceUnavailable(); // CHF → EUR
    }

    private Double extractPriceNullable(Double price) {
        return price != null ? price / 100.0 : null;
    }

    private Double averagePetrolPrice(double e5, Double superPlus) {
        if (superPlus != null) {
            return (e5 + superPlus) / 2.0;
        }
        return e5;
    }

    private double throwPriceUnavailable() {
        throw new IllegalStateException("TankKoenig price unavailable");
    }

    /**
     * API-Antwort-Objekt – nur die Felder die wir brauchen.
     */
    public record TankKoenigApiResponse(
            FuelPricesData data,
            String status
    ) {
        public record FuelPricesData(
                Double e5,
                Double e10,
                Double superPlus,
                Double diesel,
                Double ladevorgang
        ) {}
    }
}
