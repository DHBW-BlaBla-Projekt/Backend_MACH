package com.example.bookingservice.service;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class RideNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(RideNotificationConsumer.class);

    private final BookingRepository bookingRepository;

    public RideNotificationConsumer(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Höre auf stornierte Mitfahrten und aktualisiere den Buchungsstatus.
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.booking}")
    public void handleRideCancelledEvent(Map<String, Object> event) {
        if (!"RIDE_CANCELLED".equals(event.get("eventType"))) {
            log.debug("Ride-Event ignoriert, da es kein Storno-Event ist: {}", event);
            return;
        }

        Object rideIdValue = event.get("rideId");
        Long rideId = rideIdValue instanceof Number ? ((Number) rideIdValue).longValue() : null;
        log.info("Booking: RideCancelledEvent für Ride {} empfangen", rideId);

        if (rideId == null) {
            log.warn("rideId fehlt im Event: {}", event);
            return;
        }

        // Alle Buchungen für diese Mitfahrt auf CANCELLED setzen
        var bookings = bookingRepository.findByRideId(rideId);
        for (var booking : bookings) {
            if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
                booking.setStatus(Booking.BookingStatus.CANCELLED);
                bookingRepository.save(booking);
            }
        }

        log.info("Buchungen für Ride {} aktualisiert", rideId);
    }
}
