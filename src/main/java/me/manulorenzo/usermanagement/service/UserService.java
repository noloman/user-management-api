package me.manulorenzo.usermanagement.service;

import me.manulorenzo.usermanagement.dto.RegisterRequest;
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

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        logger.info("UserService initialized");
    }

    public void register(RegisterRequest request) {
        logger.debug("Starting registration process for username: {}", request.getUsername());

        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("Registration attempt with existing username: {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }

        try {
            String encodedPassword = encoder.encode(request.getPassword());
            logger.debug("Password encoded for user: {}", request.getUsername());

            // Count existing users
            long userCount = userRepo.count();
            logger.debug("Current user count: {}", userCount);

            // First user = ADMIN, others = USER
            String roleName = userCount == 0 ? "ADMIN" : "USER";
            logger.info("Assigning {} role to new user: {}", roleName, request.getUsername());

            Role role = roleRepo.findByName(roleName)
                    .orElseThrow(() -> {
                        logger.error("Role {} not found during registration for user: {}", roleName, request.getUsername());
                        return new RuntimeException("Role not found");
                    });

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(encodedPassword);
            user.setRoles(Set.of(role));

            userRepo.save(user);
            logger.info("User {} successfully registered with role: {}", request.getUsername(), roleName);

        } catch (Exception e) {
            logger.error("Error during registration for user {}: {}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    public void addRoleToUser(String username, String roleName) {
        logger.info("Starting role addition: adding {} role to user {}", roleName, username);

        try {
            User user = userRepo.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("User not found: {}", username);
                        return new UsernameNotFoundException("User not found");
                    });

            logger.debug("User {} found, current roles: {}", username, user.getRoles());

            Role role = roleRepo.findByName(roleName)
                    .orElseThrow(() -> {
                        logger.error("Role {} not found when trying to add to user {}", roleName, username);
                        return new RuntimeException("Role not found");
                    });

            if (user.getRoles().contains(role)) {
                logger.info("User {} already has role {}", username, roleName);
            } else {
                user.getRoles().add(role);
                userRepo.save(user);
                logger.info("Successfully added role {} to user {}", roleName, username);
            }

        } catch (Exception e) {
            logger.error("Error adding role {} to user {}: {}", roleName, username, e.getMessage(), e);
            throw e;
        }
    }
}
