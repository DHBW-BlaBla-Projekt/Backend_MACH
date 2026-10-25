package com.example.rideofferservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // In Spring Boot 3.x / Spring Framework 6.x, PATCH wird standardmäßig vom DispatcherServlet 
    // unterstützt. Die Fehlermeldung "Invalid HTTP method: PATCH" kommt von der Security-Konfiguration,
    // nicht von Spring MVC selbst. Diese Datei kann entfallen, wenn SecurityConfig korrekt ist.
}
