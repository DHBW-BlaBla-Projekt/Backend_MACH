package com.example.bookingservice.exception;

public class BookingEventPublishException extends RuntimeException {

    public BookingEventPublishException(String message) {
        super(message);
    }

    public BookingEventPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
