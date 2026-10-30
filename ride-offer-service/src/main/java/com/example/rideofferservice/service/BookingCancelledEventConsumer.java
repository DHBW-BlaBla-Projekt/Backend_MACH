package com.example.rideofferservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class BookingCancelledEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingCancelledEventConsumer.class);

    private final RideService rideService;

    public BookingCancelledEventConsumer(RideService rideService) {
        this.rideService = rideService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.booking-cancelled}")
    public void handleBookingCancelledEvent(Map<String, Object> event) {
        if (!"BOOKING_CANCELLED".equals(event.get("eventType"))) {
            log.debug("Event ignoriert, da es kein BookingCancelledEvent ist: {}", event);
            return;
        }

        Long rideId = toLong(event.get("rideId"));
        if (rideId == null) {
            log.warn("BookingCancelledEvent enthält keine gültige rideId: {}", event);
            return;
        }

        rideService.incrementAvailableSeats(rideId);
        log.info("Sitzplatz für stornierte Buchung freigegeben: Ride {}", rideId);
    }

    private Long toLong(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : null;
    }
}
