package com.example.profileservice.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Aktiviert @PreAuthorize für die Rating-Sicherheit.
 */
@Configuration
@EnableMethodSecurity
public class RatingSecurityConfig {
}
