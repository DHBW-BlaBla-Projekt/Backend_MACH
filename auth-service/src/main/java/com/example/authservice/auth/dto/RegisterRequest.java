package com.example.authservice.auth.dto;

/**
 * Gemeinsamer Input-DTO für Registrierung (REST + GraphQL).
 */
public record RegisterRequest(String username, String password) {}
