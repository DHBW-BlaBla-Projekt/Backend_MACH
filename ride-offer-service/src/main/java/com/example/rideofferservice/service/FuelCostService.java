package com.example.rideofferservice.service;

import com.example.rideofferservice.entity.FuelType;
import com.example.rideofferservice.entity.RideOffer;
import org.springframework.stereotype.Service;

/**
 * Berechnung der geschätzten Kraftstoffkosten für eine Fahrt.
 * Holt aktuelle Spritpreise vom FuelPriceCacheService ( TankKoenig-API mit Caching).
 */
@Service
public class FuelCostService {

    private final FuelPriceCacheService fuelPriceCacheService;

    public FuelCostService(FuelPriceCacheService fuelPriceCacheService) {
        this.fuelPriceCacheService = fuelPriceCacheService;
    }

    /**
     * Berechnet die geschätzten Kraftstoffkosten für eine Strecke.
     * Formel: (routeLengthKm / 100) * averageConsumptionLPer100km * pricePerLiter
     */
    public double calculateEstimatedFuelCost(RideOffer ride, Double routeLengthKm) {
        if (routeLengthKm == null || routeLengthKm <= 0) {
            return 0.0;
        }
        if (ride.getAverageConsumptionLPer100km() == null || ride.getAverageConsumptionLPer100km() <= 0) {
            return 0.0;
        }
        if (ride.getFuelType() == null) {
            return 0.0;
        }

        // Liter berechnen: (km / 100) * Verbrauch pro 100km
        double litersNeeded = (routeLengthKm / 100.0) * ride.getAverageConsumptionLPer100km();

        // Preis aus Cache (der ruft ggf. die TankKoenig-API auf)
        double pricePerLiter = getCurrentPrice(ride.getFuelType());

        return litersNeeded * pricePerLiter;
    }

    /**
     * Liefert den aktuellen Preis pro Liter für eine bestimmte Kraftstoffart.
     * Holt von FuelPriceCacheService (TankKoenig-API mit TTL-Caching + Fallback).
     */
    private double getCurrentPrice(FuelType fuelType) {
        return fuelPriceCacheService.getPrice(fuelType);
    }
}
