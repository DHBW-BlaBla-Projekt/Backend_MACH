package com.example.profileservice.controller;

import com.example.profileservice.model.entity.User;
import com.example.profileservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Interne Controller-Klasse für den User-Sync zwischen Auth-Service und Profile-Service.
 * <p>Dieser Endpunkt ist öffentlich (permitAll), da er vom Auth-Service aufgerufen wird,
 * der kein gültiges JWT-Token hat.</p>
 */
@RestController
@RequestMapping("/api/auth/users")
public class UserSyncController {

    private static final Logger log = LoggerFactory.getLogger(UserSyncController.class);

    private final UserRepository userRepository;

    public UserSyncController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Synchronisiert einen Benutzer vom Auth-Service in den Profile-Service.
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncUser(@RequestBody UserSyncRequest request) {
        log.info("User-Sync-Anfrage empfangen für: {}", request.getUsername());

        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "statusCode", 400,
                    "error", "Username darf nicht leer sein"
            ));
        }

        String username = request.getUsername().trim();

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setPassword(request.getPassword() != null ? request.getPassword() : "");
                    newUser.setRole(request.getRole() != null ? request.getRole() : "USER");
                    newUser.setActive(true);
                    log.info("Neuer User angelegt: {}", username);
                    return newUser;
                });

        user.setActive(true);
        userRepository.save(user);

        log.info("User {} erfolgreich synchronisiert", username);

        return ResponseEntity.ok(java.util.Map.of(
                "statusCode", 200,
                "message", "User erfolgreich synchronisiert",
                "username", user.getUsername()
        ));
    }

    public static class UserSyncRequest {
        private String username;
        private String password;
        private String role;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
