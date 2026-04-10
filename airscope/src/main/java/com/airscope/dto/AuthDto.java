package com.airscope.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTOs for Authentication endpoints.
 *
 * DTOs (Data Transfer Objects) are simple classes that carry data
 * between layers. We NEVER expose our entity classes (User, Device, etc.)
 * directly in API responses — always use DTOs.
 *
 * Why? Because entities may contain sensitive fields (like password)
 * or unnecessary database details that clients don't need.
 */
public class AuthDto {

    /**
     * Request body for POST /auth/register and POST /auth/login
     */
    @Data
    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must be at most 255 characters")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        private String password;
    }

    /**
     * Response body after successful login or registration.
     * Contains the JWT token the client will use for future requests.
     */
    @Data
    public static class AuthResponse {

        private String token;
        private String refreshToken;
        private String email;
        private String role;
        private String message;

        public AuthResponse(String token, String refreshToken, String email, String role, String message) {
            this.token = token;
            this.refreshToken = refreshToken;
            this.email = email;
            this.role = role;
            this.message = message;
        }
    }

    /**
     * Request body for refreshing tokens: POST /auth/refresh
     */
    @Data
    public static class RefreshTokenRequest {

        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }
}
