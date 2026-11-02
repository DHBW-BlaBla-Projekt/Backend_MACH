package com.example.rideofferservice.controller;

import com.example.rideofferservice.dto.CreateRideRequest;
import com.example.rideofferservice.dto.RideResponse;
import com.example.rideofferservice.entity.RideOffer;
import com.example.rideofferservice.service.RideService;
import com.example.rideofferservice.service.OrsRouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST-Controller für alle Mitfahrtsangebote.
 */
@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @GetMapping("/public/available")
    public ResponseEntity<List<RideResponse>> getAvailableRides() {
        return ResponseEntity.ok(rideService.getAvailableRides());
    }

    @GetMapping("/public/search")
    public ResponseEntity<List<RideResponse>> searchRides(
            @RequestParam String startLocation,
            @RequestParam String endLocation,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {
        List<RideResponse> rides;

        if (from != null && to != null) {
            rides = rideService.searchRidesBetween(startLocation, endLocation, from, to);
        } else if (from != null) {
            rides = rideService.searchRidesAfter(startLocation, endLocation, from);
        } else if (to != null) {
            rides = rideService.searchRidesBefore(startLocation, endLocation, to);
        } else {
            rides = rideService.searchRides(startLocation, endLocation);
        }

        return ResponseEntity.ok(rides);
    }

    @GetMapping("/my")
    public ResponseEntity<List<RideResponse>> getMyRides(Principal principal) {
        String username = principal.getName();
        return ResponseEntity.ok(rideService.getMyRides(username));
    }

    @GetMapping("/my/offered")
    public ResponseEntity<List<RideResponse>> getMyOfferedRides(Principal principal) {
        String username = principal.getName();
        return ResponseEntity.ok(rideService.getMyOfferedRides(username));
    }

    @PostMapping
    public ResponseEntity<?> createRide(
            @Valid @RequestBody CreateRideRequest request,
            Principal principal) {
        try {
            RideResponse created = rideService.createRide(principal.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (OrsRouteService.RouteCalculationException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Versucht atomar einen Sitzplatz zu buchen. 
     */
    @PatchMapping("/{id}/book")
    public ResponseEntity<?> tryBookSeat(@PathVariable Long id) {
        try {
            RideResponse updated = rideService.tryBookSeat(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Decrement available seats by 1. Called from Booking-Service after a booking.
     */
    @PatchMapping("/{id}/decrement")
    public ResponseEntity<RideResponse> decrementSeats(@PathVariable Long id) {
        RideResponse updated = rideService.decrementAvailableSeats(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Increment available seats by 1. Called from Booking-Service after a cancellation.
     */
    @PatchMapping("/{id}/increment")
    public ResponseEntity<RideResponse> incrementSeats(@PathVariable Long id) {
        RideResponse updated = rideService.incrementAvailableSeats(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RideResponse> cancelRide(
            @PathVariable Long id,
            Principal principal) {
        String username = principal.getName();
        RideResponse cancelled = rideService.cancelRide(id, username);
        return ResponseEntity.ok(cancelled);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRide(
            @PathVariable Long id,
            Principal principal) {
        String username = principal.getName();
        rideService.deleteRide(id, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideResponse> getRide(@PathVariable Long id) {
        RideResponse ride = rideService.getRideById(id);
        if (ride == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ride);
    }
}
