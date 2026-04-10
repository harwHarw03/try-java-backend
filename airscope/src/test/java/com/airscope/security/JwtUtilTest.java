package com.airscope.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "test-secret-key-must-be-at-least-32-characters-long");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationMs", 604800000L);
    }

    @Test
    void generateToken_Success() {
        String token = jwtUtil.generateToken("test@example.com", "ROLE_USER");

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void extractEmail_Success() {
        String token = jwtUtil.generateToken("test@example.com", "ROLE_USER");

        String email = jwtUtil.extractEmail(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void extractRole_Success() {
        String token = jwtUtil.generateToken("test@example.com", "ROLE_ADMIN");

        String role = jwtUtil.extractRole(token);

        assertEquals("ROLE_ADMIN", role);
    }

    @Test
    void isTokenExpired_ReturnsFalse_ForValidToken() {
        String token = jwtUtil.generateToken("test@example.com", "ROLE_USER");

        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void generateRefreshToken_DifferentFromAccessToken() {
        String accessToken = jwtUtil.generateToken("test@example.com", "ROLE_USER");
        String refreshToken = jwtUtil.generateRefreshToken("test@example.com", "ROLE_USER");

        assertNotEquals(accessToken, refreshToken);
    }
}
