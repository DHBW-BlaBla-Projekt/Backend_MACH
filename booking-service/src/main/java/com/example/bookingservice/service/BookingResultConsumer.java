package com.example.bookingservice.service;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class BookingResultConsumer {

    private final BookingRepository bookingRepository;

    public BookingResultConsumer(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.booking-result}")
    public void handleBookingResult(Map<String, Object> event) {
        Long bookingId = toLong(event.get("bookingId"));
        if (bookingId == null) {
            return;
        }

        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if ("BOOKING_CONFIRMED".equals(event.get("eventType"))) {
                booking.setStatus(Booking.BookingStatus.CONFIRMED);
            } else if ("BOOKING_REJECTED".equals(event.get("eventType"))) {
                booking.setStatus(Booking.BookingStatus.CANCELLED);
                booking.setActiveBookingKey(null);
            } else {
                return;
            }
            bookingRepository.save(booking);
        });
    }

    private Long toLong(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : null;
    }
}
