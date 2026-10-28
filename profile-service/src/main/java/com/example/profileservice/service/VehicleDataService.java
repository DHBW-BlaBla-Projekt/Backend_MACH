package com.example.profileservice.service;

import com.example.profileservice.client.FuelEconomyClient;
import com.example.profileservice.dto.VehicleDetailsDto;
import com.example.profileservice.dto.VehicleMenuItemDto;
import com.example.profileservice.dto.VehicleOptionDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class VehicleDataService {

    private static final int MIN_YEAR = 1984;
    private static final int MAX_YEAR = 2030;

    private final FuelEconomyClient fuelEconomyClient;
    private final long cacheTtlMillis;
    private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();

    public VehicleDataService(
            FuelEconomyClient fuelEconomyClient,
            @Value("${app.fuel-economy.cache-ttl-hours:24}") long cacheTtlHours) {
        this.fuelEconomyClient = fuelEconomyClient;
        this.cacheTtlMillis = Duration.ofHours(cacheTtlHours).toMillis();
    }

    public List<VehicleMenuItemDto> getYears() {
        return getCached("years", fuelEconomyClient::getYears);
    }

    public List<VehicleMenuItemDto> getMakes(Integer year) {
        validateYear(year);
        return getCached("makes:" + year, () -> fuelEconomyClient.getMakes(year));
    }

    public List<VehicleMenuItemDto> getModels(Integer year, String make) {
        validateYear(year);
        String validatedMake = validateText(make, "Marke", 100);
        return getCached("models:" + year + ":" + validatedMake.toLowerCase(),
                () -> fuelEconomyClient.getModels(year, validatedMake));
    }

    public List<VehicleOptionDto> getOptions(Integer year, String make, String model) {
        validateYear(year);
        String validatedMake = validateText(make, "Marke", 100);
        String validatedModel = validateText(model, "Modell", 150);
        return getCached("options:" + year + ":" + validatedMake.toLowerCase()
                        + ":" + validatedModel.toLowerCase(),
                () -> fuelEconomyClient.getOptions(year, validatedMake, validatedModel));
    }

    public VehicleDetailsDto getVehicleDetails(Long vehicleId) {
        if (vehicleId == null || vehicleId <= 0) {
            throw new IllegalArgumentException(
                    "Die Vehicle ID muss eine positive Zahl sein.");
        }
        return getCached("details:" + vehicleId,
                () -> fuelEconomyClient.getVehicleDetails(vehicleId));
    }

    @SuppressWarnings("unchecked")
    private <T> T getCached(String key, Supplier<T> loader) {
        CacheEntry<?> existing = cache.get(key);
        if (existing != null && !existing.expired()) {
            return (T) existing.value();
        }
        T value = loader.get();
        cache.put(key, new CacheEntry<>(value,
                System.currentTimeMillis() + cacheTtlMillis));
        return value;
    }

    private void validateYear(Integer year) {
        if (year == null) {
            throw new IllegalArgumentException("Das Baujahr ist erforderlich.");
        }
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException(
                    "Das Baujahr muss zwischen " + MIN_YEAR + " und " + MAX_YEAR + " liegen.");
        }
    }

    private String validateText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " darf nicht leer sein.");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " darf maximal " + maxLength + " Zeichen enthalten.");
        }
        return normalized;
    }

    private record CacheEntry<T>(T value, long expiresAt) {
        private boolean expired() {
            return System.currentTimeMillis() >= expiresAt;
        }
    }
}
