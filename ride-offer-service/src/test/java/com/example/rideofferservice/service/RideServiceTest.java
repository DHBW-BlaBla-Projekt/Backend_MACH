package com.example.rideofferservice.service;

import com.example.rideofferservice.client.ProfileServiceClient;
import com.example.rideofferservice.dto.CarInfoDto;
import com.example.rideofferservice.dto.CreateRideRequest;
import com.example.rideofferservice.dto.RideResponse;
import com.example.rideofferservice.entity.FuelType;
import com.example.rideofferservice.entity.RideOffer;
import com.example.rideofferservice.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RideServiceTest {

    private RideRepository rideRepository;
    private ProfileServiceClient profileServiceClient;
    private FuelCostService fuelCostService;
    private OrsRouteService orsRouteService;
    private RideService service;

    @BeforeEach
    void setUp() {
        rideRepository = mock(RideRepository.class);
        profileServiceClient = mock(ProfileServiceClient.class);
        fuelCostService = mock(FuelCostService.class);
        FuelPriceCacheService fuelPriceCacheService = mock(FuelPriceCacheService.class);
        orsRouteService = mock(OrsRouteService.class);
        service = new RideService(
                rideRepository,
                mock(CachingConnectionFactory.class),
                profileServiceClient,
                false,
                fuelCostService,
                fuelPriceCacheService,
                orsRouteService);
    }

    @Test
    void createsRideWithRouteAndEstimatedFuelCost() {
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        CreateRideRequest request = new CreateRideRequest(
                "Fulda", "Köln", departure, 2, "Testfahrt");
        request.setCarId(1L);

        when(orsRouteService.calculateRoute("Fulda", "Köln"))
                .thenReturn(new OrsRouteService.RouteResult(
                        new OrsRouteService.Coordinate(50.55, 9.67),
                        new OrsRouteService.Coordinate(50.94, 6.96),
                        "{\"type\":\"LineString\"}",
                        200.0,
                        130.0));

        CarInfoDto car = new CarInfoDto();
        car.setAverageConsumptionLPer100km(6.5);
        car.setFuelType(FuelType.PETROL);
        when(profileServiceClient.getCarById(1L)).thenReturn(car);
        when(fuelCostService.calculateEstimatedFuelCost(any(RideOffer.class), eq(200.0)))
                .thenReturn(new FuelCostService.PriceResult(25.50, null));

        RideResponse result = service.createRide("max", request);

        assertEquals("max", result.getDriverUsername());
        assertEquals(200.0, result.getRouteDistanceKm());
        assertEquals(25.50, result.getEstimatedCost());
        verify(rideRepository).save(any(RideOffer.class));
    }

    @Test
    void rejectsRideWithPastDepartureTimeBeforeCallingExternalServices() {
        CreateRideRequest request = new CreateRideRequest(
                "Fulda", "Köln", LocalDateTime.now().minusMinutes(1), 2, null);
        request.setCarId(1L);

        assertThrows(IllegalArgumentException.class,
                () -> service.createRide("max", request));

        verifyNoInteractions(orsRouteService, profileServiceClient, rideRepository);
    }
}
