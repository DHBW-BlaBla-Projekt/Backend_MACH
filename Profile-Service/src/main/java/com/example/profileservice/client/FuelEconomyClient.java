package com.example.profileservice.client;

import com.example.profileservice.dto.VehicleDetailsDto;
import com.example.profileservice.dto.VehicleMenuItemDto;
import com.example.profileservice.dto.VehicleOptionDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FuelEconomyClient {

    public List<VehicleMenuItemDto> getYears() {
        throw new UnsupportedOperationException("Noch nicht implementiert.");
    }

    public List<VehicleMenuItemDto> getMakes(int year) {
        throw new UnsupportedOperationException("Noch nicht implementiert.");
    }

    public List<VehicleMenuItemDto> getModels(int year, String make) {
        throw new UnsupportedOperationException("Noch nicht implementiert.");
    }

    public List<VehicleOptionDto> getOptions(
            int year,
            String make,
            String model) {
        throw new UnsupportedOperationException("Noch nicht implementiert.");
    }

    public VehicleDetailsDto getVehicleDetails(long vehicleId) {
        throw new UnsupportedOperationException("Noch nicht implementiert.");
    }
}
