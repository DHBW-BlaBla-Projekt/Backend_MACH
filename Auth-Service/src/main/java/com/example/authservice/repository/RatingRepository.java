package com.example.authservice.repository;

import com.example.authservice.model.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

// Datenzugriffsschicht für Ratings
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /** Prüft, ob ein bestimmter Passagier bereits einen bestimmten Fahrer bewertet hat. */
    Optional<Rating> findByPassengerUsernameAndDriverUsername(String passengerUsername, String driverUsername);

    /** Gibt alle Bewertungen eines Fahrers zurück (den dieser von Passagieren erhalten hat). */
    List<Rating> findByDriverUsername(String driverUsername);

    /** Gibt alle Bewertungen zurück, die ein bestimmter Passagier geschrieben hat. */
    List<Rating> findByPassengerUsername(String passengerUsername);

    /** Gibt alle Ratings für eine bestimmte Fahrt zurück (sollte max. 1 ergeben). */
    Optional<Rating> findByRideId(Long rideId);
}
