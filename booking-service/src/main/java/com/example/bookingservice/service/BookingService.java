package com.example.bookingservice.service;

import com.example.bookingservice.dto.BookingResponse;
import com.example.bookingservice.dto.CreateBookingRequest;
import com.example.bookingservice.dto.RideInfoDto;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.exception.BookingEventPublishException;
import com.example.bookingservice.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Value("${app.rabbitmq.routing.key.booking-cancelled:booking.cancelled.routing}")
    private String bookingCancelledRoutingKey;

    @Value("${app.rabbitmq.routing.key.booking-request:booking.requested.routing}")
    private String bookingRequestRoutingKey;

    public BookingService(BookingRepository bookingRepository,
                          RideOfferClient rideOfferClient,
                          RabbitTemplate rabbitTemplate,
                          @Value("${app.rabbitmq.enabled:false}") boolean rabbitmqEnabled) {
        this.bookingRepository = bookingRepository;
        this.rideOfferClient = rideOfferClient;
        this.rabbitmqAvailable = rabbitmqEnabled;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public BookingResponse createBooking(String username, CreateBookingRequest request) {
        if (request.getRideId() == null) {
            throw new IllegalArgumentException("Ride ID is required");
        }

        Long rideId = request.getRideId();
        if (bookingRepository.existsByRideIdAndPassengerUsernameIgnoreCaseAndStatus(
                rideId, username, Booking.BookingStatus.CONFIRMED)) {
            throw new IllegalStateException("Du hast diese Mitfahrt bereits gebucht.");
        }

        if (rabbitTemplate == null || !rabbitmqAvailable) {
            throw new BookingEventPublishException("RabbitMQ ist für Buchungen nicht verfügbar.");
        }

        Booking booking = new Booking(rideId, username);
        booking.setStatus(Booking.BookingStatus.PENDING);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Neue Buchung {} erstellt für User {}, Ride {}", 
                savedBooking.getId(), username, rideId);

        Map<String, Object> event = Map.of(
                "eventType", "BOOKING_REQUESTED",
                "bookingId", savedBooking.getId(),
                "rideId", rideId,
                "passengerUsername", username,
                "timestamp", java.time.LocalDateTime.now().toString()
        );
        try {
            rabbitTemplate.convertAndSend(exchangeName, bookingRequestRoutingKey, event);
        } catch (Exception e) {
            throw new BookingEventPublishException("Buchungsanfrage konnte nicht über RabbitMQ gesendet werden.", e);
        }

        return BookingResponse.fromEntity(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsForRide(Long rideId) {
        return bookingRepository.findByRideId(rideId)
                .stream()
                .map(this::toBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<BookingResponse> getBooking(Long bookingId, String username) {
        return bookingRepository.findByIdAndPassengerUsernameIgnoreCase(bookingId, username)
                .map(booking -> BookingResponse.fromEntity(booking, rideOfferClient.getRideById(booking.getRideId())));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(String username) {
        // Exact match for the current user's bookings with CONFIRMED status
        return bookingRepository.findByPassengerUsernameAndStatus(username, Booking.BookingStatus.CONFIRMED)
                .stream()
                .map(this::toBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByPassenger(String username) {
        return bookingRepository.findByPassengerUsernameAndStatus(username, Booking.BookingStatus.CONFIRMED)
                .stream()
                .map(this::toBookingResponse)
                .filter(booking -> booking.getDepartureTime() == null
                        || booking.getDepartureTime().isAfter(java.time.LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingHistory(String username) {
        return bookingRepository.findByPassengerUsernameAndStatus(username, Booking.BookingStatus.CONFIRMED)
                .stream()
                .map(this::toBookingResponse)
                .filter(booking -> booking.getDepartureTime() != null
                        && booking.getDepartureTime().isBefore(java.time.LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelBooking(Long bookingId, String username) {
        Booking booking = bookingRepository.findByIdAndPassengerUsernameIgnoreCase(bookingId, username)
                .orElseThrow(() -> new RuntimeException("Buchung nicht gefunden oder gehört nicht zu diesem Benutzer."));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            return;
        }

        if (booking.getStatus() == Booking.BookingStatus.PENDING) {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            booking.setActiveBookingKey(null);
            bookingRepository.save(booking);
            return;
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setActiveBookingKey(null);
        bookingRepository.save(booking);
        publishBookingCancelledEvent(booking, username);

        log.info("Buchung {} storniert von User {}", bookingId, username);
    }

    private void publishBookingCancelledEvent(Booking booking, String username) {
        if (rabbitTemplate == null || !rabbitmqAvailable) {
            throw new BookingEventPublishException(
                    "RabbitMQ ist für Buchungsstornierungen nicht verfügbar.");
        }

        try {
            Map<String, Object> event = Map.of(
                    "eventType", "BOOKING_CANCELLED",
                    "bookingId", booking.getId(),
                    "rideId", booking.getRideId(),
                    "passengerUsername", username,
                    "timestamp", java.time.LocalDateTime.now().toString()
            );
            rabbitTemplate.convertAndSend(exchangeName, bookingCancelledRoutingKey, event);
            log.info("BookingCancelledEvent gesendet: Booking {}, Ride {}",
                    booking.getId(), booking.getRideId());
        } catch (Exception e) {
            log.error("Fehler beim Senden des BookingCancelledEvents für Buchung {}: {}",
                    booking.getId(), e.getMessage(), e);
            throw new BookingEventPublishException(
                    "Buchungsstornierung konnte nicht über RabbitMQ verarbeitet werden.", e);
        }
    }

    private BookingResponse toBookingResponse(Booking booking) {
        return BookingResponse.fromEntity(booking, rideOfferClient.getRideById(booking.getRideId()));
    }
}
