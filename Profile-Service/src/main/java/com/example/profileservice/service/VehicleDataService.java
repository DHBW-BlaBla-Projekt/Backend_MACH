package com.example.profileservice.service;

import com.example.profileservice.dto.VehicleDetailsDto;
import com.example.profileservice.dto.VehicleMenuItemDto;
import com.example.profileservice.dto.VehicleOptionDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleDataService {

    private static final int MIN_YEAR = 1984;
    private static final int MAX_YEAR = 2030;

    public List<VehicleMenuItemDto> getYears() {
        return List.of();
    }

    public List<VehicleMenuItemDto> getMakes(Integer year) {
        validateYear(year);
        return List.of();
    }

    public List<VehicleMenuItemDto> getModels(Integer year, String make) {
        validateYear(year);
        String validatedMake = validateText(make, "Marke", 100);
        return List.of();
    }

    public List<VehicleOptionDto> getOptions(
            Integer year,
            String make,
            String model) {
        validateYear(year);
        String validatedMake = validateText(make, "Marke", 100);
        String validatedModel = validateText(model, "Modell", 150);
        return List.of();
    }

    public VehicleDetailsDto getVehicleDetails(Long vehicleId) {
        if (vehicleId == null || vehicleId <= 0) {
            throw new IllegalArgumentException(
                    "Die Vehicle ID muss eine positive Zahl sein.");
        }

        throw new UnsupportedOperationException(
                "Der Abruf der Fahrzeugdetails ist noch nicht implementiert.");
    }

    private void validateYear(Integer year) {
        if (year == null) {
            throw new IllegalArgumentException("Das Baujahr ist erforderlich.");
        }

        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException(
                    "Das Baujahr muss zwischen "
                            + MIN_YEAR
                            + " und "
                            + MAX_YEAR
                            + " liegen.");
        }
    }

    private String validateText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " darf nicht leer sein.");
        }

        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " darf maximal "
                            + maxLength
                            + " Zeichen enthalten.");
        }

        return normalized;
    }
}
