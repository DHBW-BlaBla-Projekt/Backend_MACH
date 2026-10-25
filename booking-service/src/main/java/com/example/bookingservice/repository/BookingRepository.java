package com.example.bookingservice.repository;

import com.example.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByRideId(Long rideId);

    /**
     * Findet Buchungen unter Berücksichtigung der Groß-/Kleinschreibung (case-insensitive).
     */
    @Query("SELECT b FROM Booking b WHERE LOWER(b.passengerUsername) = LOWER(:username)")
    List<Booking> findByPassengerUsernameIgnoreCase(@Param("username") String passengerUsername);

    /**
     * Gibt nur aktive Buchungen zurück (case-insensitive).
     */
    @Query("SELECT b FROM Booking b WHERE LOWER(b.passengerUsername) = LOWER(:username) AND b.status = :status")
    List<Booking> findByPassengerUsernameAndStatusIgnoreCase(@Param("username") String username, @Param("status") Booking.BookingStatus status);

    /**
     * Findet eine Buchung mit case-insensitive Username-Check.
     */
    @Query("SELECT b FROM Booking b WHERE b.id = :id AND LOWER(b.passengerUsername) = LOWER(:username)")
    Optional<Booking> findByIdAndPassengerUsernameIgnoreCase(@Param("id") Long id, @Param("username") String username);

    boolean existsByRideIdAndStatus(Long rideId, Booking.BookingStatus status);
}
