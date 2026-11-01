package com.example.authservice.auth.dto;

/**
 * GraphQL Input-Type für Registrierung.
 */
public class RegisterInput {
    private String username;
    private String password;

    public RegisterInput() {}

    public RegisterInput(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
