package com.example.profileservice.model.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; 


    @Column(length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String brand; // z.B. "VW"

    @Column(nullable = false, length = 50)
    private String model; // z.B. "Golf GTI"

    @Column(name = "license_plate")
    private String licensePlate;

    @Column(name = "average_consumption_l_per_100km", nullable = false)
    private Double averageConsumptionLPer100km; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FuelType fuelType; // PETROL | DIESEL | ELECTRIC | HYBRID

    public enum FuelType {
        PETROL, DIESEL, ELECTRIC, HYBRID
    }


    public Car() {}

    public Car(Long userId, String brand, String model,
               Double averageConsumptionLPer100km, FuelType fuelType) {
        this.userId = userId;
        this.brand = brand;
        this.model = model;
        this.averageConsumptionLPer100km = averageConsumptionLPer100km;
        this.fuelType = fuelType;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public Double getAverageConsumptionLPer100km() { return averageConsumptionLPer100km; }
    public void setAverageConsumptionLPer100km(Double avg) { this.averageConsumptionLPer100km = avg; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Car otherCar)) return false;
        Car car = otherCar;
        return Objects.equals(getId(), car.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
