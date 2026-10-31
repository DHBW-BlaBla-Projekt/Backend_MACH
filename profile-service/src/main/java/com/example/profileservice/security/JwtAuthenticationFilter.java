package com.example.profileservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey key;

    public JwtAuthenticationFilter(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret muss mindestens 32 Zeichen lang sein!");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        System.out.println("[JWT FILTER] Authorization header: " + (header != null ? header.substring(0, Math.min(header.length(), 50)) + "..." : "null"));

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            System.out.println("[JWT FILTER] Token length: " + token.length());
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                System.out.println("[JWT FILTER] Valid token. Username: " + username + ", Role: " + role);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String authority = "ROLE_" + (role != null ? role : "USER");

                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority(authority)
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("[JWT FILTER] Authentication set in context for: " + username);
                }
            } catch (MalformedJwtException e) {
                System.out.println("[JWT FILTER] Invalid token (Malformed): " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\": \"Invalid JWT token: " + e.getMessage() + "\"}");
                return;
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                System.out.println("[JWT FILTER] Expired token: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\": \"JWT token expired: " + e.getMessage() + "\"}");
                return;
            } catch (io.jsonwebtoken.security.SignatureException e) {
                System.out.println("[JWT FILTER] Signature mismatch: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\": \"JWT signature validation failed: " + e.getMessage() + "\"}");
                return;
            } catch (Exception e) {
                System.out.println("[JWT FILTER] Unexpected error validating token: " + e.getMessage());
            }
        } else {
            System.out.println("[JWT FILTER] No Bearer token found");
        }

        filterChain.doFilter(request, response);
    }
}
