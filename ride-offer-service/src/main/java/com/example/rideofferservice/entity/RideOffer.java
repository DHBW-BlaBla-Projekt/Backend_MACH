package com.example.rideofferservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Column(name = "car_id")
    private Long carId;

    @Column(name = "average_consumption_l_per_100km")
    private Double averageConsumptionLPer100km;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 20)
    private FuelType fuelType;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "start_latitude")
    private Double startLatitude;

    @Column(name = "start_longitude")
    private Double startLongitude;

    @Column(name = "end_latitude")
    private Double endLatitude;

    @Column(name = "end_longitude")
    private Double endLongitude;

    @Column(name = "route_geometry", columnDefinition = "TEXT")
    private String routeGeometry;

    @Column(name = "route_distance_km")
    private Double routeDistanceKm;

    @Column(name = "route_duration_minutes")
    private Double routeDurationMinutes;

    @Column(name = "estimated_cost")
    private Double estimatedCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_source", length = 20)
    private PriceSource priceSource;

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
    public Double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost; }

    public void setFulfilledAt(LocalDateTime fulfilledAt) { this.fulfilledAt = fulfilledAt; }

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }

    public Double getAverageConsumptionLPer100km() { return averageConsumptionLPer100km; }
    public void setAverageConsumptionLPer100km(Double averageConsumptionLPer100km) { this.averageConsumptionLPer100km = averageConsumptionLPer100km; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Double getStartLatitude() { return startLatitude; }
    public void setStartLatitude(Double startLatitude) { this.startLatitude = startLatitude; }

    public Double getStartLongitude() { return startLongitude; }
    public void setStartLongitude(Double startLongitude) { this.startLongitude = startLongitude; }

    public Double getEndLatitude() { return endLatitude; }
    public void setEndLatitude(Double endLatitude) { this.endLatitude = endLatitude; }

    public Double getEndLongitude() { return endLongitude; }
    public void setEndLongitude(Double endLongitude) { this.endLongitude = endLongitude; }

    public String getRouteGeometry() { return routeGeometry; }
    public void setRouteGeometry(String routeGeometry) { this.routeGeometry = routeGeometry; }

    public Double getRouteDistanceKm() { return routeDistanceKm; }
    public void setRouteDistanceKm(Double routeDistanceKm) { this.routeDistanceKm = routeDistanceKm; }

    public Double getRouteDurationMinutes() { return routeDurationMinutes; }
    public void setRouteDurationMinutes(Double routeDurationMinutes) { this.routeDurationMinutes = routeDurationMinutes; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }

    public PriceSource getPriceSource() { return priceSource; }
    public void setPriceSource(PriceSource priceSource) { this.priceSource = priceSource; }
}
