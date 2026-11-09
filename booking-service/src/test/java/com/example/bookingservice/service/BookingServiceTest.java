package com.example.bookingservice.service;

import com.example.bookingservice.dto.CreateBookingRequest;
import com.example.bookingservice.dto.RideInfoDto;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private RideOfferClient rideOfferClient;
    private BookingService service;
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        rideOfferClient = mock(RideOfferClient.class);
        rabbitTemplate = mock(RabbitTemplate.class);
        service = new BookingService(
                bookingRepository,
                rideOfferClient,
                rabbitTemplate,
                true);
        ReflectionTestUtils.setField(service, "exchangeName", "ride-offer-exchange");
        ReflectionTestUtils.setField(service, "bookingRequestRoutingKey", "booking.requested.routing");
        ReflectionTestUtils.setField(service, "bookingCancelledRoutingKey", "booking.cancelled.routing");
    }

    @Test
    void createsBookingAndReservesSeat() {
        RideInfoDto ride = availableRide();
        when(bookingRepository.existsByRideIdAndPassengerUsernameIgnoreCaseAndStatus(
                42L, "passenger", Booking.BookingStatus.CONFIRMED)).thenReturn(false);
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking booking = invocation.getArgument(0);
                    booking.setId(1L);
                    return booking;
                });

        var result = service.createBooking("passenger", new CreateBookingRequest(42L));

        assertEquals(42L, result.getRideId());
        assertEquals("passenger", result.getPassengerUsername());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void rejectsBookingForOwnRideWithoutReservingSeat() {
        when(bookingRepository.existsByRideIdAndPassengerUsernameIgnoreCaseAndStatus(
                42L, "passenger", Booking.BookingStatus.CONFIRMED)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.createBooking("passenger", new CreateBookingRequest(42L)));

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyMap());
    }

    private RideInfoDto availableRide() {
        RideInfoDto ride = new RideInfoDto();
        ride.setId(42L);
        ride.setDriverUsername("driver");
        ride.setStatus("OFFERED");
        ride.setAvailableSeats(2);
        ride.setDepartureTime(LocalDateTime.now().plusDays(1));
        return ride;
    }
}
