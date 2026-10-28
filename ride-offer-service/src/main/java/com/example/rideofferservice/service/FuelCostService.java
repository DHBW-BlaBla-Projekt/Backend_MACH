package com.example.rideofferservice.service;

import com.example.rideofferservice.entity.FuelType;
import com.example.rideofferservice.entity.PriceSource;
import com.example.rideofferservice.entity.RideOffer;
import org.springframework.stereotype.Service;

@Service
public class FuelCostService {

    private final FuelPriceCacheService fuelPriceCacheService;

    public FuelCostService(FuelPriceCacheService fuelPriceCacheService) {
        this.fuelPriceCacheService = fuelPriceCacheService;
    }

    /**
     * Berechnet die geschätzten Kraftstoffkosten und gibt Preisquelle zurück.
     */
    public PriceResult calculateEstimatedFuelCost(RideOffer ride, Double routeLengthKm) {
        if (routeLengthKm == null || routeLengthKm <= 0) {
            return new PriceResult(0.0, PriceSource.FALLBACK);
        }
        if (ride.getAverageConsumptionLPer100km() == null || ride.getAverageConsumptionLPer100km() <= 0) {
            return new PriceResult(0.0, PriceSource.FALLBACK);
        }
        if (ride.getFuelType() == null) {
            return new PriceResult(0.0, PriceSource.FALLBACK);
        }

        double litersNeeded = (routeLengthKm / 100.0) * ride.getAverageConsumptionLPer100km();
        double pricePerLiter = getCurrentPrice(ride.getFuelType());
        boolean fromApi = fuelPriceCacheService.isPriceFromApi(ride.getFuelType());

        return new PriceResult(litersNeeded * pricePerLiter, fromApi ? PriceSource.TANKKOENIG : PriceSource.FALLBACK);
    }

    private double getCurrentPrice(FuelType fuelType) {
        return fuelPriceCacheService.getPrice(fuelType);
    }

    public record PriceResult(double cost, PriceSource source) {}
}
