package com.example.authservice.graphql;

import com.example.authservice.auth.dto.AuthPayload;
import com.example.authservice.auth.dto.UserResponse;
import com.example.authservice.model.entity.User;
import com.example.authservice.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * GraphQL Query-Resolver für Authentifizierungs-Lookups.
 */
@Component
public class AuthQueryResolver {

    private final UserRepository userRepository;

    public AuthQueryResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthPayload currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Nicht authentifiziert");
        }

        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else {
            username = principal.toString();
        }

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden: " + username));

        return new AuthPayload(null, null, user.getUsername(), user.getRole(), user.isActive());
    }

    public UserResponse user(String username) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username darf nicht leer sein");
        }

        User user = userRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden: " + username));

        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.isActive());
    }
}
