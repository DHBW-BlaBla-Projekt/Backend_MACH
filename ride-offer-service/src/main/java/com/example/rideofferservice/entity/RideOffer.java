package com.example.rideofferservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(name = "ride_offers")
public class RideOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driver_username", nullable = false)
    private String driverUsername;

    @Column(nullable = false)
    private String startLocation;

    @Column(nullable = false)
    private String endLocation;

    @Column(name = "departure_datetime", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Version
    private int version;

    @Column(name = "additional_info")
    private String additionalInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RideStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "fulfilled_at")
    private LocalDateTime fulfilledAt;

    // === Phase 4.1: Neue Felder für Fahrzeugdaten ===
    @Column(name = "car_id")
    private Long carId;

    @Column(name = "average_consumption_l_per_100km")
    private Double averageConsumptionLPer100km;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 20)
    private FuelType fuelType;

    public enum RideStatus {
        OFFERED,
        FULFILLED,
        CANCELLED
    }

    public RideOffer() {}

    public RideOffer(String driverUsername, String startLocation, String endLocation, 
                     LocalDateTime departureTime, Integer availableSeats, String additionalInfo) {
        this.driverUsername = driverUsername;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.departureTime = departureTime;
        this.availableSeats = availableSeats;
        this.additionalInfo = additionalInfo;
        this.status = RideStatus.OFFERED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDriverUsername() { return driverUsername; }
    public void setDriverUsername(String driverUsername) { this.driverUsername = driverUsername; }

    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }

    public String getEndLocation() { return endLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getFulfilledAt() { return fulfilledAt; }
    public void setFulfilledAt(LocalDateTime fulfilledAt) { this.fulfilledAt = fulfilledAt; }

    // === Phase 4.1: Getter & Setter für die neuen Felder ===
    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }

    public Double getAverageConsumptionLPer100km() { return averageConsumptionLPer100km; }
    public void setAverageConsumptionLPer100km(Double averageConsumptionLPer100km) { this.averageConsumptionLPer100km = averageConsumptionLPer100km; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }
}
