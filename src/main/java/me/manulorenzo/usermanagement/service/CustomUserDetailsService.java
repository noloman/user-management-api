package me.manulorenzo.usermanagement.service;

import me.manulorenzo.usermanagement.entity.User;
import me.manulorenzo.usermanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
        logger.info("CustomUserDetailsService initialized");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user details for username: {}", username);

        try {
            User user = userRepo.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("User not found: {}", username);
                        return new UsernameNotFoundException("User not found");
                    });

            logger.debug("User {} found with {} roles", username, user.getRoles().size());

            var authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                    .toList();

            logger.debug("User {} has authorities: {}", username, authorities);

            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    user.isEnabled(), // Account enabled (email verified)
                    true, // Account not expired
                    true, // Credentials not expired
                    true, // Account not locked
                    authorities
            );

            logger.info("Successfully loaded user details for: {} (enabled: {})", username, user.isEnabled());
            return userDetails;

        } catch (UsernameNotFoundException e) {
            logger.error("Failed to load user details for username: {}", username);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error loading user details for username: {}", username, e);
            throw new UsernameNotFoundException("Error loading user details", e);
        }
    }
}
