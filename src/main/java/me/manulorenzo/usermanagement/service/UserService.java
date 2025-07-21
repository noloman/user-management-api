package me.manulorenzo.usermanagement.service;

import me.manulorenzo.usermanagement.dto.RegisterRequest;
import me.manulorenzo.usermanagement.dto.UserProfile;
import me.manulorenzo.usermanagement.entity.Role;
import me.manulorenzo.usermanagement.entity.User;
import me.manulorenzo.usermanagement.repository.RoleRepository;
import me.manulorenzo.usermanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        logger.info("UserService initialized");
    }

    public void register(RegisterRequest request) {
        logger.debug("Starting registration process for username: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("Registration attempt with existing username: {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }

        try {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            logger.debug("Password encoded for user: {}", request.getUsername());

            // Count existing users
            long userCount = userRepository.count();
            logger.debug("Current user count: {}", userCount);

            // First user = ADMIN, others = USER
            String roleName = userCount == 0 ? "ADMIN" : "USER";
            logger.info("Assigning {} role to new user: {}", roleName, request.getUsername());

            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> {
                        logger.error("Role {} not found during registration for user: {}", roleName, request.getUsername());
                        return new RuntimeException("Role not found");
                    });

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(encodedPassword);
            user.setRoles(Set.of(role));

            userRepository.save(user);
            logger.info("User {} successfully registered with role: {}", request.getUsername(), roleName);

        } catch (Exception e) {
            logger.error("Error during registration for user {}: {}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    public void addRoleToUser(String username, String roleName) {
        logger.info("Starting role addition: adding {} role to user {}", roleName, username);

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("User not found: {}", username);
                        return new UsernameNotFoundException("User not found");
                    });

            logger.debug("User {} found, current roles: {}", username, user.getRoles());

            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> {
                        logger.error("Role {} not found when trying to add to user {}", roleName, username);
                        return new RuntimeException("Role not found");
                    });

            if (user.getRoles().contains(role)) {
                logger.info("User {} already has role {}", username, roleName);
            } else {
                user.getRoles().add(role);
                userRepository.save(user);
                logger.info("Successfully added role {} to user {}", roleName, username);
            }

        } catch (Exception e) {
            logger.error("Error adding role {} to user {}: {}", roleName, username, e.getMessage(), e);
            throw e;
        }
    }

    public UserProfile getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return mapToDto(user);
    }

    public UserProfile updateUserProfile(String username, UserProfile dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Validate username changes
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
                logger.warn("Attempt to change username to existing username: {} by user: {}", dto.getUsername(), username);
                throw new RuntimeException("Username already exists");
            }
            logger.info("Username change requested from {} to {}", username, dto.getUsername());
            user.setUsername(dto.getUsername());
        }

        // Validate email changes
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                logger.warn("Attempt to change email to existing email: {} by user: {}", dto.getEmail(), username);
                throw new RuntimeException("Email already exists");
            }
            logger.info("Email change requested for user: {} to: {}", username, dto.getEmail());
            user.setEmail(dto.getEmail());
        }

        user.setFullName(dto.getFullName());
        user.setBio(dto.getBio());
        user.setImageUrl(dto.getImageUrl());

        userRepository.save(user);
        logger.info("Profile updated for user: {}", user.getUsername());
        return mapToDto(user);
    }

    private UserProfile mapToDto(User user) {
        UserProfile dto = new UserProfile();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setBio(user.getBio());
        dto.setImageUrl(user.getImageUrl());
        return dto;
    }
}
