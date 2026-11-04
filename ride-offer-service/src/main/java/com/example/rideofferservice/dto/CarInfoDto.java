package com.example.rideofferservice.dto;

import com.example.rideofferservice.entity.FuelType;

/**
 * DTO für Car-Daten vom Profile-Service. KI_LOG : 2
 */
public class CarInfoDto {
    private Long id;
    private String name;
    private String brand;
    private Double averageConsumptionLPer100km;
    private FuelType fuelType;

    public CarInfoDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Double getAverageConsumptionLPer100km() { return averageConsumptionLPer100km; }
    public void setAverageConsumptionLPer100km(Double averageConsumptionLPer100km) { this.averageConsumptionLPer100km = averageConsumptionLPer100km; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }
}
