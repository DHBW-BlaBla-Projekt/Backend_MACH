package com.example.profileservice.service;

import com.example.profileservice.client.FuelEconomyClient;
import com.example.profileservice.dto.VehicleMenuItemDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class VehicleDataServiceTest {

    @Test
    void cachesValidatedRequests() {
        FuelEconomyClient client = mock(FuelEconomyClient.class);
        when(client.getMakes(2020)).thenReturn(List.of(new VehicleMenuItemDto("BMW", "BMW")));
        VehicleDataService service = new VehicleDataService(client, 24);

        assertEquals(1, service.getMakes(2020).size());
        assertEquals(1, service.getMakes(2020).size());
        verify(client, times(1)).getMakes(2020);
    }

    @Test
    void validatesInputBeforeCallingVehicleClient() {
        FuelEconomyClient client = mock(FuelEconomyClient.class);
        VehicleDataService service = new VehicleDataService(client, 24);

        assertThrows(IllegalArgumentException.class,
                () -> service.getModels(1983, "Volkswagen"));
        verifyNoInteractions(client);
    }
}
