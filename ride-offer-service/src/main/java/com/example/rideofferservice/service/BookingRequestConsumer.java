package com.example.rideofferservice.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class BookingRequestConsumer {

    private final RideService rideService;
    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String resultRoutingKey;

    public BookingRequestConsumer(
            RideService rideService,
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.exchange.name}") String exchangeName,
            @Value("${app.rabbitmq.routing.key.booking-result}") String resultRoutingKey) {
        this.rideService = rideService;
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.resultRoutingKey = resultRoutingKey;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.booking-request}")
    public void handleBookingRequest(Map<String, Object> event) {
        Long bookingId = toLong(event.get("bookingId"));
        Long rideId = toLong(event.get("rideId"));
        if (bookingId == null || rideId == null) {
            return;
        }

        String eventType = "BOOKING_CONFIRMED";
        try {
            rideService.tryBookSeat(rideId);
        } catch (RuntimeException e) {
            eventType = "BOOKING_REJECTED";
        }

        rabbitTemplate.convertAndSend(exchangeName, resultRoutingKey, Map.of(
                "eventType", eventType,
                "bookingId", bookingId,
                "rideId", rideId
        ));
    }

    private Long toLong(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : null;
    }
}
