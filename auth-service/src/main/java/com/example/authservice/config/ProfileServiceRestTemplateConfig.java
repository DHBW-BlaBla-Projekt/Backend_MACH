package com.example.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Konfiguriert den RestTemplate für die Kommunikation mit dem Profile-Service.
 */
@Configuration
public class ProfileServiceRestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
