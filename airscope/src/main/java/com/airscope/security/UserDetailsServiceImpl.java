package com.airscope.security;

import com.airscope.model.User;
import com.airscope.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserDetailsServiceImpl - tells Spring Security how to load a user.
 *
 * Spring Security needs to know how to find a user by their username (email in our case).
 * We implement UserDetailsService and override loadUserByUsername().
 *
 * This is called by JwtAuthFilter during every authenticated request.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load a user by email address.
     * Spring Security uses this to verify the token and check permissions.
     *
     * @param email the user's email
     * @return UserDetails object that Spring Security understands
     * @throws UsernameNotFoundException if the user doesn't exist
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find the user in PostgreSQL
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Convert our User entity to Spring Security's UserDetails format
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole())) // e.g., "ROLE_USER"
        );
    }
}
