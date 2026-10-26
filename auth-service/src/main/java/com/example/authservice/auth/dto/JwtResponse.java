package com.example.authservice.auth.dto;


//DTO
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String role;
    private boolean active;

    public JwtResponse() {}

    public JwtResponse(String accessToken, String refreshToken, String username, String role, boolean active) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
        this.active = active;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
}
