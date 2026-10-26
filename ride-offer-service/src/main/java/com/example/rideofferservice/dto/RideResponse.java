package com.example.rideofferservice.dto;

import com.example.rideofferservice.entity.RideOffer;
import java.time.LocalDateTime;

public class RideResponse {

    private Long id;
    private String driverUsername;
    private String startLocation;
    private String endLocation;
    private LocalDateTime departureTime;
    private Integer availableSeats;
    private String additionalInfo;
    private RideOffer.RideStatus status;

    // === Phase 4.3: Neue Felder für Fahrzeugdaten ===
    private Long carId;
    private Double averageConsumptionLPer100km;
    private String fuelType;

    public RideResponse() {}

    public RideResponse(Long id, String driverUsername, String startLocation, String endLocation, 
                        LocalDateTime departureTime, Integer availableSeats, String additionalInfo, 
                        RideOffer.RideStatus status) {
        this.id = id;
        this.driverUsername = driverUsername;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.departureTime = departureTime;
        this.availableSeats = availableSeats;
        this.additionalInfo = additionalInfo;
        this.status = status;
    }

    // === Phase 4.3: Konstruktur mit allen neuen Feldern ===
    public RideResponse(Long id, String driverUsername, String startLocation, String endLocation, 
                        LocalDateTime departureTime, Integer availableSeats, String additionalInfo, 
                        RideOffer.RideStatus status, Long carId, Double averageConsumptionLPer100km, String fuelType) {
        this.id = id;
        this.driverUsername = driverUsername;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.departureTime = departureTime;
        this.availableSeats = availableSeats;
        this.additionalInfo = additionalInfo;
        this.status = status;
        this.carId = carId;
        this.averageConsumptionLPer100km = averageConsumptionLPer100km;
        this.fuelType = fuelType;
    }

    public static RideResponse fromEntity(RideOffer entity) {
        return new RideResponse(
            entity.getId(),
            entity.getDriverUsername(),
            entity.getStartLocation(),
            entity.getEndLocation(),
            entity.getDepartureTime(),
            entity.getAvailableSeats(),
            entity.getAdditionalInfo(),
            entity.getStatus()
        );
    }

    // === Phase 4.3: Overloaded fromEntity für die neuen Felder ===
    public static RideResponse fromEntityFull(RideOffer entity) {
        return new RideResponse(
            entity.getId(),
            entity.getDriverUsername(),
            entity.getStartLocation(),
            entity.getEndLocation(),
            entity.getDepartureTime(),
            entity.getAvailableSeats(),
            entity.getAdditionalInfo(),
            entity.getStatus(),
            entity.getCarId(),
            entity.getAverageConsumptionLPer100km(),
            entity.getFuelType()
        );
    }

    public Long getId() { return id; }
    public String getDriverUsername() { return driverUsername; }
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public Integer getAvailableSeats() { return availableSeats; }
    public String getAdditionalInfo() { return additionalInfo; }
    public RideOffer.RideStatus getStatus() { return status; }

    // === Phase 4.3: Getter & Setter für die neuen Felder ===
    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }

    public Double getAverageConsumptionLPer100km() { return averageConsumptionLPer100km; }
    public void setAverageConsumptionLPer100km(Double averageConsumptionLPer100km) { this.averageConsumptionLPer100km = averageConsumptionLPer100km; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
}
