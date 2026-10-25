package com.example.bookingservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // Diese Konfiguration ist notwendig, um sicherzustellen, 
    // dass Spring MVC die Methode "PATCH" für interne Requests unterstützt.
}
