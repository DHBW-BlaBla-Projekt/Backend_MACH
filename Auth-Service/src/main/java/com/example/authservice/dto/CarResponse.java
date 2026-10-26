package com.example.authservice.dto;

import com.example.authservice.model.entity.Car;

public class CarResponse {

    private Long id;
    private String brand;
    private String model;
    private Double averageConsumptionLPer100km;
    private String fuelType;

    public CarResponse() {}

    public static CarResponse fromEntity(Car car) {
        CarResponse response = new CarResponse();
        response.setId(car.getId());
        response.setBrand(car.getBrand());
        response.setModel(car.getModel());
        response.setAverageConsumptionLPer100km(car.getAverageConsumptionLPer100km());
        response.setFuelType(car.getFuelType().name());
        return response;
    }

    public Long getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public Double getAverageConsumptionLPer100km() { return averageConsumptionLPer100km; }
    public String getFuelType() { return fuelType; }
}