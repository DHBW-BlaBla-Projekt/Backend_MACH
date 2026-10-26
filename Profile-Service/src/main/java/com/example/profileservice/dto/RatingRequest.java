package com.example.profileservice.dto;

/**
 * Request DTO für das Erstellen einer neuen Bewertung.
 */
public class RatingRequest {

    /** ID der Fahrt, die bewertet werden soll */
    private Long rideId;

    /** Username des Fahrers (wer bewertet wird) */
    private String driverUsername;

    /** Sternbewertung: 1 bis 5 */
    private int rating;

    /** Optionaler Freitext-Kommentar */
    private String comment;

    public RatingRequest() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getDriverUsername() { return driverUsername; }
    public void setDriverUsername(String driverUsername) { this.driverUsername = driverUsername; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
