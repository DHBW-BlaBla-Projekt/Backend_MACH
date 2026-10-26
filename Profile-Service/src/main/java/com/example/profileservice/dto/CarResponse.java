package com.example.profileservice.dto;

import com.example.profileservice.model.entity.Car;

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
    public void setId(Long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Double getAverageConsumptionLPer100km() { return averageConsumptionLPer100km; }
    public void setAverageConsumptionLPer100km(Double averageConsumptionLPer100km) { this.averageConsumptionLPer100km = averageConsumptionLPer100km; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
}
