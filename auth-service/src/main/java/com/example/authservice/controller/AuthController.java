package com.example.authservice.controller;

import com.example.authservice.auth.JwtTokenProvider;
import com.example.authservice.auth.dto.JwtResponse;
import com.example.authservice.auth.dto.LoginRequest;
import com.example.authservice.client.ProfileServiceClient;
import com.example.authservice.model.entity.RefreshToken;
import com.example.authservice.model.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.ratelimit.RateLimiter;
import com.example.authservice.ratelimit.RateLimitResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST-Controller für alle Authentifizierungs-Endpunkte.
 *
 * <p>Dient der Benutzerregistrierung, dem Login mit JWT-Token-Ausstellung,
 * dem Refresh von Tokens sowie dem Logout.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final com.example.authservice.service.RefreshTokenService refreshTokenService;
    private final ProfileServiceClient profileServiceClient;
    private final RateLimiter rateLimiter;

    /**
     * Konstruktor mit Dependency Injection.
     */
    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            com.example.authservice.service.RefreshTokenService refreshTokenService,
            ProfileServiceClient profileServiceClient,
            RateLimiter rateLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.profileServiceClient = profileServiceClient;
        this.rateLimiter = rateLimiter;
    }


    /**
     * Öffentlicher Endpunkt: Registriert einen neuen Benutzer im System.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "statusCode", 400,
                    "error", "Username darf nicht leer sein!"));
        }


        if (!rateLimiter.tryConsume(username, rateLimiter.getAttemptsByUsernameMap())) {
            return ResponseEntity.status(429).body(RateLimitResponse.ofUsername(username));
        }

        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            // Failed registration counts against the limit – schon durch tryConconsume gezählt!
            return ResponseEntity.badRequest().body(Map.of(
                    "statusCode", 400,
                    "error", "Username bereits vergeben!"));
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "statusCode", 400,
                    "error", "Passwort darf nicht leer sein!"
            ));
        }

        if (request.getPassword().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of(
                    "statusCode", 400,
                    "error", "Passwort muss mindestens 8 Zeichen lang sein!"
            ));
        }


        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");  // Immer USER

        userRepository.save(user);

        rateLimiter.recordSuccess(username, rateLimiter.getAttemptsByUsernameMap());

        try {
            profileServiceClient.syncUser(user.getUsername(), request.getPassword(), user.getRole());
        } catch (Exception e) {
        }

        return ResponseEntity.ok(Map.of(
                "statusCode", 201,
                "message", "User erfolgreich registriert!"));
    }


    /**
     * Öffentlicher Endpunkt: Login eines Benutzers mit Username und Passwort.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();


        if (!rateLimiter.tryConsume(username, rateLimiter.getAttemptsByUsernameMap())) {
            return ResponseEntity.status(429).body(RateLimitResponse.ofUsername(username));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {

            return ResponseEntity.status(401).body(Map.of(
                    "statusCode", 401,
                    "error", "Ungültiger Benutzername oder Passwort!"));
        }


        rateLimiter.recordSuccess(username, rateLimiter.getAttemptsByUsernameMap());

        User user = userRepository.findByUsernameIgnoreCase(request.getUsername()).orElseThrow();
        

        try {
            profileServiceClient.syncUser(user.getUsername(), "", user.getRole());
        } catch (Exception e) {

        }

        String accessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new JwtResponse(
                accessToken,
                refreshToken.getToken(),
                user.getUsername(),
                user.getRole(),
                user.isActive()
        ));
    }


    /**
     * Geschützter Endpunkt: Erneuert den AccessToken mit einem gültigen Refresh-Token.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of(
                    "statusCode", 401,
                    "error", "Fehlender oder ungültiger Authorization-Header"));
        }

        String token = authHeader.substring(7);

        try {
            RefreshToken refreshToken = refreshTokenService.validateRefreshToken(token);
            User user = refreshToken.getUser();

            String newAccessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRole());

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", refreshToken.getToken()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "statusCode", 401,
                    "error", e.getMessage()));
        }
    }


    /**
     * Geschützter Endpunkt: Logout – widerruft das aktuelle Refresh-Token.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of(
                    "statusCode", 401,
                    "error", "Fehlender oder ungültiger Authorization-Header"));
        }

        String token = authHeader.substring(7);
        refreshTokenService.revokeRefreshToken(token);

        return ResponseEntity.ok(Map.of(
                "statusCode", 200,
                "message", "Erfolgreich ausgeloggt!"));
    }

    /**
     * DTO für Registrierungsanfragen.
     */
    public static class RegisterRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * DTO für Login-Anfragen.
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
