package com.example.bookingservice.service;

import com.example.bookingservice.dto.CreateBookingRequest;
import com.example.bookingservice.dto.RideInfoDto;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private RideOfferClient rideOfferClient;
    private BookingService service;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        rideOfferClient = mock(RideOfferClient.class);
        service = new BookingService(
                bookingRepository,
                rideOfferClient,
                mock(CachingConnectionFactory.class),
                false);
    }

    @Test
    void createsBookingAndReservesSeat() {
        RideInfoDto ride = availableRide();
        when(rideOfferClient.getRideById(42L)).thenReturn(ride);
        when(bookingRepository.existsByRideIdAndPassengerUsernameIgnoreCaseAndStatus(
                42L, "passenger", Booking.BookingStatus.CONFIRMED)).thenReturn(false);
        when(rideOfferClient.tryBookSeat(42L)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createBooking("passenger", new CreateBookingRequest(42L));

        assertEquals(42L, result.getRideId());
        assertEquals("passenger", result.getPassengerUsername());
        verify(rideOfferClient).tryBookSeat(42L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void rejectsBookingForOwnRideWithoutReservingSeat() {
        RideInfoDto ride = availableRide();
        ride.setDriverUsername("driver");
        when(rideOfferClient.getRideById(42L)).thenReturn(ride);

        assertThrows(IllegalStateException.class,
                () -> service.createBooking("DRIVER", new CreateBookingRequest(42L)));

        verify(rideOfferClient, never()).tryBookSeat(any());
        verify(bookingRepository, never()).save(any());
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
