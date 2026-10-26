package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class CarRequest {

    @NotBlank(message = "Marke ist erforderlich")
    private String brand;

    @NotBlank(message = "Modell ist erforderlich")
    private String model;

    @NotNull(message = "Durchschnittsverbrauch ist erforderlich")
    @PositiveOrZero(message = "Verbrauch darf nicht negativ sein")
    private Double averageConsumptionLPer100km;

    @NotBlank(message = "Treibstoffart ist erforderlich")
    private String fuelType; // PETROL, DIESEL, ELECTRIC, HYBRID

    public CarRequest() {}

    public CarRequest(String brand, String model, Double averageConsumptionLPer100km, String fuelType) {
        this.brand = brand;
        this.model = model;
        this.averageConsumptionLPer100km = averageConsumptionLPer100km;
        this.fuelType = fuelType;
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Double getAverageConsumptionLPer100km() { return averageConsumptionLPer100km; }
    public void setAverageConsumptionLPer100km(Double averageConsumptionLPer100km) { this.averageConsumptionLPer100km = averageConsumptionLPer100km; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
}
