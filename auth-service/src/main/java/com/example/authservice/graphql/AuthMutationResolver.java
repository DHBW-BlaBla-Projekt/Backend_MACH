package com.example.authservice.graphql;

import com.example.authservice.auth.JwtTokenProvider;
import com.example.authservice.auth.dto.AuthPayload;
import com.example.authservice.auth.dto.LoginRequest;
import com.example.authservice.auth.dto.RegisterRequest;
import com.example.authservice.client.ProfileServiceClient;
import com.example.authservice.model.entity.RefreshToken;
import com.example.authservice.model.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.ratelimit.RateLimiter;
import com.example.authservice.service.RefreshTokenService;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * GraphQL-Mutation-Resolver für Authentifizierungsoperationen.
 * Nutzt die shared DTOs aus auth.dto-Paket.
 */
@Component
public class AuthMutationResolver {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final ProfileServiceClient profileServiceClient;
    private final RateLimiter rateLimiter;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AuthMutationResolver(
            UserRepository userRepository,
            RefreshTokenService refreshTokenService,
            JwtTokenProvider tokenProvider,
            AuthenticationManager authenticationManager,
            ProfileServiceClient profileServiceClient,
            RateLimiter rateLimiter,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.profileServiceClient = profileServiceClient;
        this.rateLimiter = rateLimiter;
        this.passwordEncoder = passwordEncoder;
    }

    @MutationMapping
    public MutationResult register(RegisterRequest input) {
        String username = input.username() == null ? "" : input.username().trim();
        String password = input.password();

        List<String> errors = validateRegister(username, password);
        if (!errors.isEmpty()) {
            return new MutationResult(false, null, errors);
        }

        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return new MutationResult(false, null, List.of("Username bereits vergeben!"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        userRepository.save(user);

        rateLimiter.recordSuccess(username, rateLimiter.getAttemptsByUsernameMap());

        try {
            profileServiceClient.syncUser(user.getUsername(), password, user.getRole());
        } catch (Exception e) {
            // non-fatal: auth works even if profile sync fails
        }

        AuthPayload data = new AuthPayload(null, null, username, "USER", false);
        return new MutationResult(true, data, List.of());
    }

    @MutationMapping
    public MutationResult login(LoginRequest input) {
        String username = input.username() == null ? "" : input.username().trim();
        String password = input.password();

        List<String> errors = validateLogin(username, password);
        if (!errors.isEmpty()) {
            return new MutationResult(false, null, errors);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException e) {
            return new MutationResult(false, null, List.of("Ungültiger Benutzername oder Passwort!"));
        }

        rateLimiter.recordSuccess(username, rateLimiter.getAttemptsByUsernameMap());

        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow();

        try {
            profileServiceClient.syncUser(user.getUsername(), "", user.getRole());
        } catch (Exception e) {
            // non-fatal: auth works even if profile sync fails
        }

        String accessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        AuthPayload data = new AuthPayload(
                accessToken,
                refreshToken.getToken(),
                user.getUsername(),
                user.getRole(),
                user.isActive());
        return new MutationResult(true, data, List.of());
    }

    @MutationMapping
    public MutationResult refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return new MutationResult(false, null, List.of("Fehlender Refresh-Token"));
        }

        try {
            RefreshToken rt = refreshTokenService.validateRefreshToken(refreshToken);
            User user = rt.getUser();
            String newAccessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRole());

            AuthPayload data = new AuthPayload(
                    newAccessToken,
                    refreshToken,
                    user.getUsername(),
                    user.getRole(),
                    user.isActive());
            return new MutationResult(true, data, List.of());
        } catch (RuntimeException e) {
            return new MutationResult(false, null, List.of(e.getMessage()));
        }
    }

    @MutationMapping
    public Boolean logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Fehlender Refresh-Token");
        }
        refreshTokenService.revokeRefreshToken(refreshToken);
        return true;
    }

    private List<String> validateRegister(String username, String password) {
        var list = new ArrayList<String>();
        if (username == null || username.isEmpty()) {
            list.add("Username darf nicht leer sein!");
        }
        if (password == null || password.isEmpty()) {
            list.add("Passwort darf nicht leer sein!");
        } else if (password.length() < 8) {
            list.add("Passwort muss mindestens 8 Zeichen lang sein!");
        }
        return list;
    }

    private List<String> validateLogin(String username, String password) {
        var list = new ArrayList<String>();
        if (username == null || username.isEmpty()) {
            list.add("Username darf nicht leer sein!");
        }
        if (password == null || password.isEmpty()) {
            list.add("Passwort darf nicht leer sein!");
        }
        return list;
    }

    // ==================== Shared GraphQL Types ====================

    public static class MutationResult {
        private final boolean success;
        private final AuthPayload data;
        private final List<String> errors;

        public MutationResult() { this(false, null, List.of()); }

        public MutationResult(boolean success, AuthPayload data, List<String> errors) {
            this.success = success;
            this.data = data;
            this.errors = errors != null ? errors : List.of();
        }

        public boolean isSuccess() { return success; }
        public AuthPayload getData() { return data; }
        public List<String> getErrors() { return errors; }
    }
}
