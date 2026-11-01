package com.example.authservice.controller;

import com.example.authservice.auth.JwtTokenProvider;
import com.example.authservice.auth.dto.AuthPayload;
import com.example.authservice.auth.dto.LoginRequest;
import com.example.authservice.auth.dto.RegisterRequest;
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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String username = request.username() == null ? "" : request.username().trim();
        if (username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "statusCode", 400,
                    "error", "Username darf nicht leer sein!"));
        }

        if (!rateLimiter.tryConsume(username, rateLimiter.getAttemptsByUsernameMap())) {
            return ResponseEntity.status(429).body(RateLimitResponse.ofUsername(username));
        }

        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "statusCode", 400,
                    "error", "Username bereits vergeben!"));
        }

        if (request.password() == null || request.password().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "statusCode", 400,
                    "error", "Passwort darf nicht leer sein!"));
        }

        if (request.password().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of(
                    "statusCode", 400,
                    "error", "Passwort muss mindestens 8 Zeichen lang sein!"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        userRepository.save(user);

        rateLimiter.recordSuccess(username, rateLimiter.getAttemptsByUsernameMap());

        try {
            profileServiceClient.syncUser(user.getUsername(), request.password(), user.getRole());
        } catch (Exception e) {
            // non-fatal: auth works even if profile sync fails
        }

        return ResponseEntity.ok(Map.of(
                "statusCode", 201,
                "message", "User erfolgreich registriert!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String username = request.username() == null ? "" : request.username().trim();

        if (!rateLimiter.tryConsume(username, rateLimiter.getAttemptsByUsernameMap())) {
            return ResponseEntity.status(429).body(RateLimitResponse.ofUsername(username));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "statusCode", 401,
                    "error", "Ungültiger Benutzername oder Passwort!"));
        }

        rateLimiter.recordSuccess(username, rateLimiter.getAttemptsByUsernameMap());

        User user = userRepository.findByUsernameIgnoreCase(request.username()).orElseThrow();

        try {
            profileServiceClient.syncUser(user.getUsername(), "", user.getRole());
        } catch (Exception e) {
            // non-fatal: auth works even if profile sync fails
        }

        String accessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new AuthPayload(
                accessToken,
                refreshToken.getToken(),
                user.getUsername(),
                user.getRole(),
                user.isActive()));
    }

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

            return ResponseEntity.ok(new AuthPayload(
                    newAccessToken,
                    refreshToken.getToken(),
                    user.getUsername(),
                    user.getRole(),
                    user.isActive()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "statusCode", 401,
                    "error", e.getMessage()));
        }
    }

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
}
