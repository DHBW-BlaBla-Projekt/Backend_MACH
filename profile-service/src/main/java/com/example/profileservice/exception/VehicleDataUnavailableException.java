package com.example.profileservice.exception;

public class VehicleDataUnavailableException extends RuntimeException {

    public VehicleDataUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
