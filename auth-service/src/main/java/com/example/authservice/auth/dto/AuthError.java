package com.example.authservice.auth.dto;

/**
 * GraphQL Error-Typ für Validierungsfehler.
 */
public class AuthError {
    private String field;
    private String message;

    public AuthError() {}

    public AuthError(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
