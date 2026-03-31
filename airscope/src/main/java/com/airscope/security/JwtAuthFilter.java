package com.airscope.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtAuthFilter - intercepts every HTTP request and validates the JWT token.
 *
 * How it works:
 * 1. Client sends: GET /devices  with header: Authorization: Bearer eyJhbGci...
 * 2. This filter runs BEFORE the controller
 * 3. It extracts the token from the header
 * 4. It validates the token
 * 5. If valid, it tells Spring Security "this user is authenticated"
 * 6. Spring Security then allows the request to reach the controller
 *
 * OncePerRequestFilter = this runs exactly once per request (not multiple times)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain // the rest of the filter chain
    ) throws ServletException, IOException {

        // 1. Get the Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2. If there's no Authorization header or it doesn't start with "Bearer ", skip this filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // pass to next filter
            return;
        }

        // 3. Extract the token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        try {
            // 4. Get the user's email from the token
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            // 5. Only proceed if we got an email AND the user isn't already authenticated
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 7. Validate the token
                if (jwtUtil.isTokenValid(token, userDetails)) {

                    // 8. Create an authentication object with the user's role
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // no credentials needed after token validation
                            List.of(new SimpleGrantedAuthority(role)) // user's role/permissions
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 9. Tell Spring Security this request is authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token is invalid, expired, or tampered — just don't authenticate
            // The request will be rejected by Spring Security with 401 Unauthorized
            logger.warn("JWT validation failed: " + e.getMessage());
        }

        // 10. Continue to the next filter/controller
        filterChain.doFilter(request, response);
    }
}
