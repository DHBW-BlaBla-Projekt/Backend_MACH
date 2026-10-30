package com.example.bookingservice.service;

import com.example.bookingservice.dto.BookingResponse;
import com.example.bookingservice.dto.CreateBookingRequest;
import com.example.bookingservice.dto.RideInfoDto;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final RideOfferClient rideOfferClient;
    private final RabbitTemplate rabbitTemplate;
    private final boolean rabbitmqAvailable;

    @Value("${app.rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing.key.booking:booking.routing}")
    private String bookingRoutingKey;

    public BookingService(BookingRepository bookingRepository,
                          RideOfferClient rideOfferClient,
                          org.springframework.amqp.rabbit.connection.CachingConnectionFactory connectionFactory,
                          @Value("${app.rabbitmq.enabled:false}") boolean rabbitmqEnabled) {
        this.bookingRepository = bookingRepository;
        this.rideOfferClient = rideOfferClient;

        boolean available = false;
        if (rabbitmqEnabled) {
            try (var conn = connectionFactory.createConnection()) {
                available = true;
            } catch (Exception e) {
                log.warn("RabbitMQ nicht verfügbar: {}", e.getMessage());
            }
        }

        this.rabbitmqAvailable = available;

        org.springframework.amqp.rabbit.core.RabbitTemplate template;
        try {
            if (available) {
                template = new RabbitTemplate(connectionFactory);
                template.setMessageConverter(new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter());
            } else {
                template = null;
            }
        } catch (Exception e) {
            log.warn("RabbitTemplate konnte nicht erstellt werden.");
            template = null;
        }
        this.rabbitTemplate = template;
    }

    @Transactional
    public BookingResponse createBooking(String username, CreateBookingRequest request) {
        if (request.getRideId() == null) {
            throw new IllegalArgumentException("Ride ID is required");
        }

        Long rideId = request.getRideId();
        RideInfoDto rideInfo = rideOfferClient.getRideById(rideId);
        
        if (rideInfo == null) {
            throw new IllegalArgumentException("Mitfahrt nicht gefunden: " + rideId);
        }

        if (rideInfo.getDriverUsername() != null
                && rideInfo.getDriverUsername().equalsIgnoreCase(username)) {
            throw new IllegalStateException("Du kannst deine eigene Fahrt nicht buchen.");
        }

        if (bookingRepository.existsByRideIdAndPassengerUsernameIgnoreCaseAndStatus(
                rideId, username, Booking.BookingStatus.CONFIRMED)) {
            throw new IllegalStateException("Du hast diese Mitfahrt bereits gebucht.");
        }

        String status = rideInfo.getStatus();
        if (!"OFFERED".equals(status)) {
            throw new IllegalStateException("Die Mitfahrt ist nicht mehr verfügbar. Status: " + status);
        }

        java.time.LocalDateTime departureTime = rideInfo.getDepartureTime();
        if (departureTime != null && departureTime.isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("Die Abfahrtszeit liegt bereits in der Vergangenheit.");
        }


        Integer availableSeats = rideInfo.getAvailableSeats();
        if (availableSeats == null || availableSeats <= 0) {
            throw new IllegalStateException("Keine freien Plätze mehr verfügbar für diese Mitfahrt.");
        }


        if (!rideOfferClient.tryBookSeat(rideId)) { 
            throw new IllegalStateException("Keine freien Plätze mehr verfügbar für diese Mitfahrt."); 
        } 

        Booking booking = new Booking(rideId, username); 
        Booking savedBooking = bookingRepository.save(booking); 

        log.info("Neue Buchung {} erstellt für User {}, Ride {}", 
                savedBooking.getId(), username, rideId);

        if (rabbitTemplate != null) {
            try {
                Map<String, Object> event = Map.of(
                    "bookingId", savedBooking.getId(),
                    "rideId", rideId,
                    "passengerUsername", username,
                    "timestamp", java.time.LocalDateTime.now().toString()
                );
                rabbitTemplate.convertAndSend(exchangeName, bookingRoutingKey + ".created", event);
            } catch (Exception e) {
                log.warn("Fehler beim Senden von BookingCreatedEvent: {}", e.getMessage());
            }
        }

        return BookingResponse.fromEntity(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsForRide(Long rideId) {
        return bookingRepository.findByRideId(rideId)
                .stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(String username) {
        // Exact match for the current user's bookings with CONFIRMED status
        return bookingRepository.findByPassengerUsernameAndStatus(username, Booking.BookingStatus.CONFIRMED)
                .stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByPassenger(String username) {
        // Exact match for all bookings by passenger
        return bookingRepository.findByPassengerUsername(username)
                .stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelBooking(Long bookingId, String username) {
        Booking booking = bookingRepository.findByIdAndPassengerUsernameIgnoreCase(bookingId, username)
                .orElseThrow(() -> new RuntimeException("Buchung nicht gefunden oder gehört nicht zu diesem Benutzer."));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Buchung wurde bereits storniert.");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setActiveBookingKey(null);
        bookingRepository.save(booking);

        // Freigebe den Sitz wieder im Ride-Offer-Service
        rideOfferClient.incrementAvailableSeats(booking.getRideId());


        if (rabbitTemplate != null) {
            try {
                Map<String, Object> event = Map.of(
                    "bookingId", booking.getId(),
                    "rideId", booking.getRideId(),
                    "passengerUsername", booking.getPassengerUsername(),
                    "timestamp", java.time.LocalDateTime.now().toString()
                );
                rabbitTemplate.convertAndSend(exchangeName, bookingRoutingKey + ".cancelled", event);
            } catch (Exception e) {
                log.warn("Fehler beim Senden von BookingCancelledEvent: {}", e.getMessage());
            }
        }

        log.info("Buchung {} storniert von User {}", bookingId, username);
    }
}
