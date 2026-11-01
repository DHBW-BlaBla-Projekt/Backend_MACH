package com.example.authservice.auth.dto;

/**
 * Gemeinsamer Output-DTO für Authentifizierungs-Antworten (REST + GraphQL).
 */
public class AuthPayload {
    private String accessToken;
    private String refreshToken;
    private final String username;
    private final String role;
    private final boolean active;

    public AuthPayload() {
        this(null, null, "", "", false);
    }

    public AuthPayload(String accessToken, String refreshToken, String username, String role, boolean active) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username != null ? username : "";
        this.role = role != null ? role : "USER";
        this.active = active;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getUsername() { return username; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
}
