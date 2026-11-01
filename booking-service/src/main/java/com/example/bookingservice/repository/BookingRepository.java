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
    List<Booking> findByPassengerUsername(@Param("username") String passengerUsername);

    /**
     * Gibt nur aktive Buchungen zurück (case-insensitive).
     */
    @Query("SELECT b FROM Booking b WHERE LOWER(b.passengerUsername) = LOWER(:username) AND b.status = :status")
    List<Booking> findByPassengerUsernameAndStatus(@Param("username") String username, @Param("status") Booking.BookingStatus status);

    /**
     * Findet eine Buchung mit case-insensitive Username-Check.
     */
    @Query("SELECT b FROM Booking b WHERE b.id = :id AND LOWER(b.passengerUsername) = LOWER(:username)")
    Optional<Booking> findByIdAndPassengerUsernameIgnoreCase(@Param("id") Long id, @Param("username") String username);

    boolean existsByRideIdAndStatus(Long rideId, Booking.BookingStatus status);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
           "FROM Booking b WHERE b.rideId = :rideId " +
           "AND LOWER(b.passengerUsername) = LOWER(:username) " +
           "AND b.status = :status")
    boolean existsByRideIdAndPassengerUsernameIgnoreCaseAndStatus(
            @Param("rideId") Long rideId,
            @Param("username") String username,
            @Param("status") Booking.BookingStatus status);

    long countByRideIdAndStatus(Long rideId, Booking.BookingStatus status);
}
