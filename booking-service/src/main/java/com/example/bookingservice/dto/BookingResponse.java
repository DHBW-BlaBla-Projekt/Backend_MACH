package com.example.bookingservice.dto;

import com.example.bookingservice.entity.Booking;
import java.time.LocalDateTime;
// KI_LOG : 2
public class BookingResponse {

    private Long id;
    private Long rideId;
    private String passengerUsername;
    private Booking.BookingStatus status;
    private LocalDateTime createdAt;
    private String startLocation;
    private String endLocation;

    public BookingResponse() {}

    public BookingResponse(Long id, Long rideId, String passengerUsername, 
                           Booking.BookingStatus status, LocalDateTime createdAt) {
        this(id, rideId, passengerUsername, status, createdAt, null, null);
    }

    public BookingResponse(Long id, Long rideId, String passengerUsername,
                           Booking.BookingStatus status, LocalDateTime createdAt,
                           String startLocation, String endLocation) {
        this.id = id;
        this.rideId = rideId;
        this.passengerUsername = passengerUsername;
        this.status = status;
        this.createdAt = createdAt;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public static BookingResponse fromEntity(Booking entity) {
        return fromEntity(entity, null);
    }

    public static BookingResponse fromEntity(Booking entity, RideInfoDto rideInfo) {
        return new BookingResponse(
            entity.getId(),
            entity.getRideId(),
            entity.getPassengerUsername(),
            entity.getStatus(),
            entity.getCreatedAt(),
            rideInfo != null ? rideInfo.getStartLocation() : null,
            rideInfo != null ? rideInfo.getEndLocation() : null
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getPassengerUsername() { return passengerUsername; }
    public void setPassengerUsername(String passengerUsername) { this.passengerUsername = passengerUsername; }

    public Booking.BookingStatus getStatus() { return status; }
    public void setStatus(Booking.BookingStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }

    public String getEndLocation() { return endLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
}
