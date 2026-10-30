package com.example.profileservice.exception;

public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(Long vehicleId) {
        super("Fahrzeug mit der ID " + vehicleId + " wurde nicht gefunden.");
    }
}
