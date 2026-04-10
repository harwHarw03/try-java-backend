package com.airscope.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtUtil - handles all JWT (JSON Web Token) operations.
 *
 * What is a JWT?
 * A JWT is a signed string that contains user information (like email and role).
 * When a user logs in, we give them a JWT. They send it with every request,
 * and we verify it without needing to hit the database each time.
 *
 * Structure: header.payload.signature
 * Example:   eyJhbGci...  .  eyJzdWIi...  .  SflKxwRJ...
 */
@Component
public class JwtUtil {

    // Read from application.properties
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    /**
     * Get the signing key from our secret string.
     * We must use a cryptographically strong key.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate an access token for a user.
     *
     * @param email the user's email (will be the "subject" of the token)
     * @param role  the user's role (stored as a "claim" inside the token)
     * @return signed JWT string
     */
    public String generateToken(String email, String role) {
        return generateToken(email, role, expirationMs);
    }

    /**
     * Generate a refresh token for a user (longer expiration).
     */
    public String generateRefreshToken(String email, String role) {
        return generateToken(email, role, refreshExpirationMs);
    }

    private String generateToken(String email, String role, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract the email (subject) from a token.
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extract the role from a token.
     */
    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    /**
     * Check if a token is still valid (not expired, and belongs to the right user).
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Check if the token's expiration date has passed.
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Parse the token and return all claims (the payload data).
     * This will throw an exception if the token is invalid or tampered with.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
