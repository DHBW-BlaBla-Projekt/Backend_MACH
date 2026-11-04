package com.example.rideofferservice.dto;

import com.example.rideofferservice.entity.FuelType;
import com.example.rideofferservice.entity.RideOffer;
import java.time.LocalDateTime;

//KI_LOG : 2
public class RideResponse {

    private Long id;
    private String driverUsername;
    private String startLocation;
    private String endLocation;
    private LocalDateTime departureTime;
    private Integer availableSeats;
    private String additionalInfo;
    private RideOffer.RideStatus status;
    private Long carId;
    private Double averageConsumptionLPer100km;
    private FuelType fuelType;
    private Double estimatedCost;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private String routeGeometry;
    private Double routeDistanceKm;
    private Double routeDurationMinutes;

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

    public RideResponse(Long id, String driverUsername, String startLocation, String endLocation, 
                        LocalDateTime departureTime, Integer availableSeats, String additionalInfo, 
                        RideOffer.RideStatus status, Long carId, Double averageConsumptionLPer100km, FuelType fuelType, Double estimatedCost) {
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
        this.estimatedCost = estimatedCost;
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

    public static RideResponse fromEntityFull(RideOffer entity) {
        RideResponse response = new RideResponse(
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
            entity.getFuelType(),
            entity.getEstimatedCost()
        );
        response.startLatitude = entity.getStartLatitude();
        response.startLongitude = entity.getStartLongitude();
        response.endLatitude = entity.getEndLatitude();
        response.endLongitude = entity.getEndLongitude();
        response.routeGeometry = entity.getRouteGeometry();
        response.routeDistanceKm = entity.getRouteDistanceKm();
        response.routeDurationMinutes = entity.getRouteDurationMinutes();
        return response;
    }

    public Long getId() { return id; }
    public String getDriverUsername() { return driverUsername; }
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public Integer getAvailableSeats() { return availableSeats; }
    public String getAdditionalInfo() { return additionalInfo; }
    public RideOffer.RideStatus getStatus() { return status; }


    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }

    public Double getAverageConsumptionLPer100km() { return averageConsumptionLPer100km; }
    public void setAverageConsumptionLPer100km(Double averageConsumptionLPer100km) { this.averageConsumptionLPer100km = averageConsumptionLPer100km; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }

    public Double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost; }

    public Double getStartLatitude() { return startLatitude; }
    public Double getStartLongitude() { return startLongitude; }
    public Double getEndLatitude() { return endLatitude; }
    public Double getEndLongitude() { return endLongitude; }
    public String getRouteGeometry() { return routeGeometry; }
    public Double getRouteDistanceKm() { return routeDistanceKm; }
    public Double getRouteDurationMinutes() { return routeDurationMinutes; }

}
