package com.example.profileservice.dto;

import java.time.LocalDateTime;

/**
 * Response DTO für eine Bewertung. KI_LOG : 2
 */
public class RatingResponse {

    private Long id;
    private String passengerUsername;
    private Long rideId;
    private String driverUsername;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public RatingResponse() {}

    /** Erstellt eine Response aus einer Rating-Entity. */
    public RatingResponse(Long id, String passengerUsername, Long rideId,
                          String driverUsername, int rating, String comment,
                          LocalDateTime createdAt) {
        this.id = id;
        this.passengerUsername = passengerUsername;
        this.rideId = rideId;
        this.driverUsername = driverUsername;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPassengerUsername() { return passengerUsername; }
    public void setPassengerUsername(String passengerUsername) { this.passengerUsername = passengerUsername; }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getDriverUsername() { return driverUsername; }
    public void setDriverUsername(String driverUsername) { this.driverUsername = driverUsername; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
