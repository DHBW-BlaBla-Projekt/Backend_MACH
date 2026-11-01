package com.example.authservice.auth.dto;

/**
 * Gemeinsamer Output-DTO für Benutzerprofil-Anzeigen (GraphQL).
 */
public class UserResponse {
    private final long id;
    private final String username;
    private final String role;
    private final boolean active;

    public UserResponse() { this(0, "", "", false); }

    public UserResponse(long id, String username, String role, boolean active) {
        this.id = id;
        this.username = username != null ? username : "";
        this.role = role != null ? role : "USER";
        this.active = active;
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
}
