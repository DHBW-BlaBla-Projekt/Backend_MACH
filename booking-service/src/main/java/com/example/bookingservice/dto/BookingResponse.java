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

    public BookingResponse() {}

    public BookingResponse(Long id, Long rideId, String passengerUsername, 
                           Booking.BookingStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.rideId = rideId;
        this.passengerUsername = passengerUsername;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static BookingResponse fromEntity(Booking entity) {
        return new BookingResponse(
            entity.getId(),
            entity.getRideId(),
            entity.getPassengerUsername(),
            entity.getStatus(),
            entity.getCreatedAt()
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
}
