package com.example.bookingservice.service;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service zur Validierung, ob eine Bewertung erlaubt ist.
 * Wird vom Auth-Service per HTTP-Aufruf angesprochen.
 */
@Service
public class RatingValidationService {

    private static final Logger log = LoggerFactory.getLogger(RatingValidationService.class);

    private final BookingRepository bookingRepository;
    private final RideOfferClient rideOfferClient;

    public RatingValidationService(BookingRepository bookingRepository, RideOfferClient rideOfferClient) {
        this.bookingRepository = bookingRepository;
        this.rideOfferClient = rideOfferClient;
    }

    /**
     * Prüft, ob ein Passagier die Berechtigung hat, den Fahrer für eine bestimmte Fahrt zu bewerten.
     * 
     * Bewertung nur erlaubt wenn:
     * 1. Eine Buchung mit dieser rideId existiert
     * 2. Der passengerUsername (case-insensitive) korrekt ist
     * 3. Der Status 'CONFIRMED' ist (Bewertung nach der Fahrt)
     */
    public boolean isValidRating(Long rideId, String passengerUsername) {
        java.util.List<Booking> bookings = bookingRepository.findByRideId(rideId);
        
        for (Booking booking : bookings) {
            if (booking.getStatus() == Booking.BookingStatus.CONFIRMED 
                    && passengerUsername.equalsIgnoreCase(booking.getPassengerUsername())) {
                var ride = rideOfferClient.getRideById(rideId);
                if (ride == null || ride.getDepartureTime() == null
                        || ride.getDepartureTime().isAfter(java.time.LocalDateTime.now())) {
                    continue;
                }
                log.debug("Bewertung erlaubt für rideId={}, passenger={}", rideId, passengerUsername);
                return true;
            }
        }

        log.debug("Bewertung NICHT erlaubt für rideId={}, passenger={} – keine gültige Buchung gefunden", 
                rideId, passengerUsername);
        return false;
    }
}
