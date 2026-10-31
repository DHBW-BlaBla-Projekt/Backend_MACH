package com.example.profileservice.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entität für eine Bewertung eines Fahrers durch einen Passagier.
 * Unique-Constraint: Jeder Passagier kann pro Fahrt genau once bewerten.
 * Ein Passagier darf also mehrere Bewertungen abgeben, wenn es verschiedene Fahrten sind — auch für denselben Driver.
 */
@Entity
@Table(name = "ratings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"passenger_username", "ride_id"}))
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rating_seq")
    @SequenceGenerator(name = "rating_seq", sequenceName = "ratings_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "passenger_username", nullable = false, length = 50)
    private String passengerUsername;

    @Column(name = "ride_id", nullable = false)
    private Long rideId;

    @Column(name = "driver_username", nullable = false, length = 50)
    private String driverUsername;


    @Column(nullable = false)
    private int rating;

    @Column(length = 500)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Rating() {}

    /** Erstellt eine neue Bewertung und setzt das Erstellungsdatum automatisch. */
    public Rating(String passengerUsername, Long rideId, String driverUsername, int rating, String comment) {
        this.passengerUsername = passengerUsername;
        this.rideId = rideId;
        this.driverUsername = driverUsername;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
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
