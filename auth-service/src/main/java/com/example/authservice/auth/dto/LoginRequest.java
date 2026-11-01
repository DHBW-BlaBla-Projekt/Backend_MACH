package com.example.authservice.auth.dto;

/**
 * Gemeinsamer Input-DTO für Login (REST + GraphQL).
 */
public record LoginRequest(String username, String password) {}
