package com.example.authservice.graphql;

import com.example.authservice.auth.JwtTokenProvider;
import com.example.authservice.client.ProfileServiceClient;
import com.example.authservice.model.entity.RefreshToken;
import com.example.authservice.model.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.ratelimit.RateLimiter;
import com.example.authservice.service.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL-Controller für Auth-Mutationen.
 * Die Input/Output-Typen werden über das SDL-Schema definiert.
 */
@Controller
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

    /**
     * GraphQL-Mutation: register(input: RegisterInput!) → AuthPayload | AuthError
     */
    @graphql.GraphQLMutation(name = "register")
    public Object register(AuthMutationResolver.RegisterInput input) {
        String username = input.getUsername() == null ? "" : input.getUsername().trim();
        String password = input.getPassword();

        List<String> errors = validateRegister(username, password);
        if (!errors.isEmpty()) {
            return new AuthError(errors);
        }

        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return new AuthError(List.of("Username bereits vergeben!"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        userRepository.save(user);

        rateLimiter.recordSuccess(username, rateLimiter.getAttemptsByUsernameMap());

        try { profileServiceClient.syncUser(user.getUsername(), password, user.getRole()); } catch (Exception e) { /* non-fatal */ }

        return new AuthPayload(null, null, username, "USER", false);
    }

    /**
     * GraphQL-Mutation: login(input: LoginInput!) → AuthPayload | AuthError
     */
    @graphql.GraphQLMutation(name = "login")
    public Object login(AuthMutationResolver.LoginInput input) {
        String username = input.getUsername() == null ? "" : input.getUsername().trim();
        String password = input.getPassword();

        List<String> errors = validateLogin(username, password);
        if (!errors.isEmpty()) {
            return new AuthError(errors);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException e) {
            return new AuthError(List.of("Ungültiger Benutzername oder Passwort!"));
        }

        rateLimiter.recordSuccess(username, rateLimiter.getAttemptsByUsernameMap());

        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow();

        try { profileServiceClient.syncUser(user.getUsername(), "", user.getRole()); } catch (Exception e) { /* non-fatal */ }

        String accessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthPayload(accessToken, refreshToken.getToken(), user.getUsername(), user.getRole(), user.isActive());
    }

    /**
     * GraphQL-Mutation: refreshToken(refreshToken: String!) → AuthPayload | AuthError
     */
    @graphql.GraphQLMutation(name = "refreshToken")
    public Object refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return new AuthError(List.of("Fehlender Refresh-Token"));
        }

        try {
            RefreshToken rt = refreshTokenService.validateRefreshToken(refreshToken);
            User user = rt.getUser();
            String newAccessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRole());
            return new AuthPayload(newAccessToken, refreshToken, user.getUsername(), user.getRole(), user.isActive());
        } catch (RuntimeException e) {
            return new AuthError(List.of(e.getMessage()));
        }
    }

    /**
     * GraphQL-Mutation: logout(refreshToken: String!) → Boolean | AuthError
     */
    @graphql.GraphQLMutation(name = "logout")
    public Object logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return new AuthError(List.of("Fehlender Refresh-Token"));
        }
        refreshTokenService.revokeRefreshToken(refreshToken);
        return true;
    }

    // --- Validation helpers ---

    private List<String> validateRegister(String username, String password) {
        var list = new java.util.ArrayList<String>();
        if (username == null || username.isEmpty()) list.add("Username darf nicht leer sein!");
        if (password == null || password.isEmpty()) list.add("Passwort darf nicht leer sein!");
        if (password != null && password.length() < 8) list.add("Passwort muss mindestens 8 Zeichen lang sein!");
        return list;
    }

    private List<String> validateLogin(String username, String password) {
        var list = new java.util.ArrayList<String>();
        if (username == null || username.isEmpty()) list.add("Username darf nicht leer sein!");
        if (password == null || password.isEmpty()) list.add("Passwort darf nicht leer sein!");
        return list;
    }

    // --- GraphQL Input/Output types (mapped via SDL) ---

    public record RegisterInput(String username, String password) {}

    public record LoginInput(String username, String password) {}

    public static class AuthPayload {
        private String accessToken;
        private String refreshToken;
        private String username;
        private String role;
        private boolean active;

        public AuthPayload() {}

        public AuthPayload(String accessToken, String refreshToken, String username, String role, boolean active) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.username = username;
            this.role = role;
            this.active = active;
        }

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public boolean isActive() { return active; }
    }

    public static class AuthError {
        private List<String> errors;
        public AuthError() {}
        public AuthError(List<String> errors) { this.errors = errors; }
        public List<String> getErrors() { return errors; }
    }
}
