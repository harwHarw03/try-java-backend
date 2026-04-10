package com.airscope.service;

import com.airscope.dto.AuthDto.AuthResponse;
import com.airscope.dto.AuthDto.LoginRequest;
import com.airscope.model.User;
import com.airscope.repository.UserRepository;
import com.airscope.security.JwtUtil;
import com.airscope.util.AppExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService - handles user registration and login business logic.
 *
 * This is the SERVICE layer: it contains business logic only.
 * It does NOT handle HTTP requests (that's the controller's job).
 * It does NOT query the database directly (that's the repository's job).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user.
     *
     * Steps:
     *   1. Check if email is already taken
     *   2. Hash the password (NEVER store plain text passwords)
     *   3. Save the user to PostgreSQL
     *   4. Generate and return a JWT token
     */
    public AuthResponse register(LoginRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppExceptions.BadRequestException("Email is already registered: " + request.getEmail());
        }

        // Build the new user entity
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt hash
                .role("ROLE_USER") // default role for new registrations
                .build();

        userRepository.save(newUser);

        // Generate JWT tokens immediately so they don't need to log in separately
        String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(newUser.getEmail(), newUser.getRole());

        return new AuthResponse(token, refreshToken, newUser.getEmail(), newUser.getRole(), "Registration successful");
    }

    /**
     * Login an existing user.
     *
     * Steps:
     *   1. Use Spring Security's AuthenticationManager to verify credentials
     *   2. If credentials are wrong, it throws BadCredentialsException (handled globally)
     *   3. If correct, generate and return JWT tokens
     */
    public AuthResponse login(LoginRequest request) {
        // This does the heavy lifting: loads the user, compares BCrypt hashes
        // Throws BadCredentialsException if email/password don't match
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // If we get here, credentials were correct — load user to get their role
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getRole());

        return new AuthResponse(token, refreshToken, user.getEmail(), user.getRole(), "Login successful");
    }

    /**
     * Refresh tokens using a valid refresh token.
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new AppExceptions.UnauthorizedException("Refresh token has expired");
        }

        String email = jwtUtil.extractEmail(refreshToken);
        String role = jwtUtil.extractRole(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getRole());

        return new AuthResponse(newToken, newRefreshToken, user.getEmail(), user.getRole(), "Token refreshed");
    }
}
