package com.example.authservice.controller;

import com.example.authservice.auth.JwtTokenProvider;
import com.example.authservice.auth.dto.LoginRequest;
import com.example.authservice.auth.dto.RegisterRequest;
import com.example.authservice.client.ProfileServiceClient;
import com.example.authservice.model.entity.RefreshToken;
import com.example.authservice.model.entity.User;
import com.example.authservice.ratelimit.RateLimiter;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider tokenProvider;
    private RefreshTokenService refreshTokenService;
    private ProfileServiceClient profileServiceClient;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        tokenProvider = mock(JwtTokenProvider.class);
        refreshTokenService = mock(RefreshTokenService.class);
        profileServiceClient = mock(ProfileServiceClient.class);
        controller = new AuthController(
                userRepository,
                passwordEncoder,
                authenticationManager,
                tokenProvider,
                refreshTokenService,
                profileServiceClient,
                new RateLimiter());
    }

    @Test
    void registersUserWithEncodedPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(" max ");
        request.setPassword("secret123");
        when(userRepository.findByUsernameIgnoreCase("max")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");

        var response = controller.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("max")
                        && user.getPassword().equals("hashed-password")
                        && user.getRole().equals("USER")));
        verify(profileServiceClient).syncUser("max", "secret123", "USER");
    }

    @Test
    void rejectsDuplicateUsernameWithoutSaving() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("max");
        request.setPassword("secret123");
        when(userRepository.findByUsernameIgnoreCase("max"))
                .thenReturn(Optional.of(new User()));

        var response = controller.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void returnsTokensForValidLogin() {
        User user = new User("max", "hashed-password", "USER", true);
        RefreshToken refreshToken = new RefreshToken(
                "refresh-token", Instant.now().plusSeconds(3600), user);
        when(userRepository.findByUsernameIgnoreCase("max")).thenReturn(Optional.of(user));
        when(tokenProvider.generateAccessToken("max", "USER")).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        var response = controller.login(new LoginRequest("max", "secret123"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationManager).authenticate(any());
        verify(refreshTokenService).createRefreshToken(user);
    }
}
