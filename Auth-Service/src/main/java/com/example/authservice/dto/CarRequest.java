package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class CreateCarRequest {

    @NotBlank(message = "Marke ist erforderlich")
    private String brand;

    @NotBlank(message = "Modell ist erforderlich")
    private String model;

    @NotNull(message = "Durchschnittsverbrauch ist erforderlich")
    @PositiveOrZero(message = "Verbrauch darf nicht negativ sein")
    private Double averageConsumptionLPer100km;

    @NotBlank(message = "Treibstoffart ist erforderlich")
    private String fuelType; // PETROL, DIESEL, ELECTRIC, HYBRID

    public CreateCarRequest() {}

    public CreateCarRequest(String brand, String model, Double averageConsumptionLPer100km, String fuelType) {
        this.brand = brand;
        this.model = model;
        this.averageConsumptionLPer100km = averageConsumptionLPer100km;
        this.fuelType = fuelType;
    }

    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public Double getAverageConsumptionLPer100km() { return averageConsumptionLPer100km; }
    public String getFuelType() { return fuelType; }
}