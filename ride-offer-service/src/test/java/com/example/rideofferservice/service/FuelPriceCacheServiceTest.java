package com.example.rideofferservice.service;

import com.example.rideofferservice.client.TankKoenigClient;
import com.example.rideofferservice.entity.FuelType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FuelPriceCacheServiceTest {

    @Test
    void cachesSuccessfulApiResponse() {
        TankKoenigClient client = mock(TankKoenigClient.class);
        when(client.fetchAveragePrices()).thenReturn(
                Map.of(FuelType.PETROL, 1.79, FuelType.DIESEL, 1.69));
        FuelPriceCacheService service = new FuelPriceCacheService(client);

        assertEquals(1.79, service.getPrice(FuelType.PETROL));
        assertEquals(1.79, service.getPrice(FuelType.PETROL));
        assertTrue(service.isPriceFromApi(FuelType.PETROL));

        verify(client).fetchAveragePrices();
    }

    @Test
    void keepsFallbackAndDoesNotRetryForEveryRequestWhenApiFails() {
        TankKoenigClient client = mock(TankKoenigClient.class);
        when(client.fetchAveragePrices()).thenThrow(new IllegalStateException("upstream unavailable"));
        FuelPriceCacheService service = new FuelPriceCacheService(client);

        assertEquals(1.85, service.getPrice(FuelType.PETROL));
        assertEquals(1.85, service.getPrice(FuelType.PETROL));
        assertFalse(service.isPriceFromApi(FuelType.PETROL));

        verify(client).fetchAveragePrices();
    }
}
