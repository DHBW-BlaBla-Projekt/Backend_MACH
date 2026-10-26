package com.example.rideofferservice.service;

import com.example.rideofferservice.entity.RideOffer;
import org.springframework.stereotype.Service;

/**
 * Phase 5: Berechnung der geschätzten Kraftstoffkosten für eine Fahrt.
 */
@Service
public class FuelCostService {

    /**
     * Berechnet die geschätzten Kraftstoffkosten für eine Strecke.
     *
     * Formel: (routeLengthKm / 100) * averageConsumptionLPer100km * pricePerLiter
     */
    public double calculateEstimatedFuelCost(RideOffer ride, Double routeLengthKm) {
        if (routeLengthKm == null || routeLengthKm <= 0) {
            return 0.0;
        }
        if (ride.getAverageConsumptionLPer100km() == null || ride.getAverageConsumptionLPer100km() <= 0) {
            return 0.0;
        }
        if (ride.getFuelType() == null || ride.getFuelType().isBlank()) {
            return 0.0;
        }

        // Liter berechnen: (km / 100) * Verbrauch pro 100km
        double litersNeeded = (routeLengthKm / 100.0) * ride.getAverageConsumptionLPer100km();

        // Preis abrufen (aus Cache oder externer API) — hier Platzhalter
        double pricePerLiter = getCurrentPrice(ride.getFuelType());

        return litersNeeded * pricePerLiter;
    }

    /**
     * Liefert den aktuellen Preis pro Liter für eine bestimmte Kraftstoffart.
     * In der Produktion würde dies aus einem Cache oder einer externen API kommen.
     */
    private double getCurrentPrice(String fuelType) {
        // TODO: Hier später die FuelPriceConfig-Logik einbauen (Phase 5.1/5.2)
        // Platzhalter-Werte (in EUR), können später über eine Config/API ersetzt werden:
        switch (fuelType.toUpperCase()) {
            case "PETROL":
                return 1.85;
            case "DIESEL":
                return 1.70;
            case "ELECTRIC":
                return 0.45; // pro kWh-Äquivalent in EUR
            case "HYBRID":
                return 1.75;
            default:
                return 1.80; // Standard
        }
    }
}
