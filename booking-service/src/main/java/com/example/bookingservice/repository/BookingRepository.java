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

    List<Booking> findByPassengerUsername(String passengerUsername);

    /**
     * Gibt nur aktive Buchungen zurück.
     */
    @Query("SELECT b FROM Booking b WHERE b.passengerUsername = :username AND b.status = :status")
    List<Booking> findByPassengerUsernameAndStatus(@Param("username") String username, @Param("status") Booking.BookingStatus status);

    Optional<Booking> findByIdAndPassengerUsername(Long id, String passengerUsername);

    boolean existsByRideIdAndStatus(Long rideId, Booking.BookingStatus status);
}
