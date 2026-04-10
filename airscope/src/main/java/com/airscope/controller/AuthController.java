package com.airscope.controller;

import com.airscope.dto.AuthDto.AuthResponse;
import com.airscope.dto.AuthDto.LoginRequest;
import com.airscope.dto.AuthDto.RefreshTokenRequest;
import com.airscope.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - handles user registration and login.
 *
 * These endpoints are PUBLIC — no JWT token required.
 * (Configured in SecurityConfig)
 *
 * Base URL: /api/v1/auth
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/register
     *
     * Register a new user account.
     *
     * Request body:
     * {
     *   "email": "user@example.com",
     *   "password": "mypassword"
     * }
     *
     * Response (201 Created):
     * {
     *   "token": "eyJhbGci...",
     *   "email": "user@example.com",
     *   "role": "ROLE_USER",
     *   "message": "Registration successful"
     * }
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /auth/login
     *
     * Login with existing credentials.
     *
     * Request body:
     * {
     *   "email": "user@example.com",
     *   "password": "mypassword"
     * }
     *
     * Response (200 OK):
     * {
     *   "token": "eyJhbGci...",
     *   "refreshToken": "eyJhbGci...",
     *   "email": "user@example.com",
     *   "role": "ROLE_USER",
     *   "message": "Login successful"
     * }
     */
    @PostMapping("/login")
    @Operation(summary = "Login and get a JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/refresh
     *
     * Refresh access token using a valid refresh token.
     *
     * Request body:
     * {
     *   "refreshToken": "eyJhbGci..."
     * }
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}
