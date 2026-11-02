package com.example.rideofferservice.graphql;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class GraphQLSecurityConfig {

    @Bean
    public SecurityFilterChain graphQLSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/graphql**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
