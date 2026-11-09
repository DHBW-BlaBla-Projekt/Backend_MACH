package com.example.bookingservice.controller;

import com.example.bookingservice.dto.BookingResponse;
import com.example.bookingservice.dto.CreateBookingRequest;
import com.example.bookingservice.exception.BookingEventPublishException;
import com.example.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;

/**
 * REST-Controller für alle Buchungs-Endpunkte.
 *
 * <p>Dient der Bereitstellung von Endpunkten zur Erstellung, Suche und Löschung von
 * Buchungen. Alle Endpunkte erfordern eine Authentifizierung.</p>
 *
 * <h2>Verwendete Endpunkte:</h2>
 * <ul>
 *   <li>{@code POST /api/bookings} – Erstellen einer neuen Buchung</li>
 *   <li>{@code GET /api/bookings/ride/{rideId}} – Abrufen aller Buchungen für eine Mitfahrt</li>
 *   <li>{@code GET /api/bookings/my-bookings} – Abrufen der eigenen Buchungen</li>
 *   <li>{@code DELETE /api/bookings/{bookingId}} – Stornieren einer Buchung</li>
 * </ul>
 *
 * @see com.example.bookingservice.service.BookingService
 * @see com.example.bookingservice.entity.Booking
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;


    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody CreateBookingRequest request, Authentication authentication) {
        try {
            BookingResponse booking = bookingService.createBooking(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(booking);
        } catch (BookingEventPublishException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Du hast diese Mitfahrt bereits gebucht."));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Interner Fehler: " + e.getMessage()));
        }

    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBooking(@PathVariable Long bookingId, Authentication authentication) {
        return bookingService.getBooking(bookingId, authentication.getName())
                .map(booking -> ResponseEntity.ok(booking))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<BookingResponse>> getBookingsForRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(bookingService.getBookingsForRide(rideId));
    }


    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication authentication) {
        return ResponseEntity.ok(bookingService.getBookingsByPassenger(authentication.getName()));
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<BookingResponse>> getMyBookingHistory(Authentication authentication) {
        return ResponseEntity.ok(bookingService.getBookingHistory(authentication.getName()));
    }


    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId, Authentication authentication) {
        try {
            bookingService.cancelBooking(bookingId, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Buchung erfolgreich gelöscht!"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (BookingEventPublishException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
