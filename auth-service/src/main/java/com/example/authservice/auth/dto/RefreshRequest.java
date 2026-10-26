package com.example.authservice.auth.dto;

//DTO für Refresh-Anfrage – der Token kommt aus dem Header, hier nur als Platzhalter für Body-Optional
public class RefreshRequest {
    private String refreshToken;

    public RefreshRequest() {}

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
