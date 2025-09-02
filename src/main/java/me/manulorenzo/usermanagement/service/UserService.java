package me.manulorenzo.usermanagement.service;

import me.manulorenzo.usermanagement.dto.ForgotPasswordRequest;
import me.manulorenzo.usermanagement.dto.RegisterRequest;
import me.manulorenzo.usermanagement.dto.ResetPasswordRequest;
import me.manulorenzo.usermanagement.dto.UserProfile;
import me.manulorenzo.usermanagement.dto.VerifyEmailRequest;
import me.manulorenzo.usermanagement.entity.Role;
import me.manulorenzo.usermanagement.entity.User;
import me.manulorenzo.usermanagement.repository.RoleRepository;
import me.manulorenzo.usermanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final String adminRoleName;
    private final String userRoleName;
    private final boolean firstUserAdmin;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(
            @Value("${app.roles.admin}") String adminRoleName,
            @Value("${app.roles.user}") String userRoleName,
            @Value("${app.security.first-user-admin}") boolean firstUserAdmin,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.adminRoleName = adminRoleName;
        this.userRoleName = userRoleName;
        this.firstUserAdmin = firstUserAdmin;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;

        logger.info("UserService initialized with admin role: '{}', user role: '{}', first-user-admin: {}",
                adminRoleName, userRoleName, firstUserAdmin);
    }

    public void register(RegisterRequest request) {
        logger.debug("Starting registration process for username: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("Registration attempt with existing username: {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Registration attempt with existing email: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        try {
            // Create new user (disabled until email verification)
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEnabled(false); // Disabled until email verified
            user.setEmailVerified(false);

            // Generate verification token
            user.setVerificationToken(UUID.randomUUID().toString());
            user.setVerificationTokenExpiry(Instant.now().plusSeconds(24 * 60 * 60)); // 24 hours

            // Assign role
            long userCount = userRepository.count();
            logger.debug("Current user count: {}", userCount);

            // Determine role based on configuration
            String roleName = (userCount == 0 && firstUserAdmin) ? adminRoleName : userRoleName;
            logger.info("Assigning {} role to new user: {}", roleName, request.getUsername());

            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> {
                        logger.error("Role {} not found during registration for user: {}", roleName, request.getUsername());
                        return new RuntimeException("Role not found");
                    });

            user.getRoles().add(role);

            // Save user
            user = userRepository.save(user);
            logger.info("User '{}' registered successfully (disabled, pending email verification)", user.getUsername());

            // Send verification email
            emailService.queueVerificationEmail(user);

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

    public String verifyEmail(VerifyEmailRequest request) {
        logger.info("Attempting to verify email for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if already verified
        if (user.isEmailVerified()) {
            logger.info("Email already verified for user: {}", user.getUsername());
            return "Email already verified";
        }

        // Check token
        if (!request.getToken().equals(user.getVerificationToken())) {
            logger.warn("Invalid verification token for user: {}", user.getUsername());
            throw new RuntimeException("Invalid verification token");
        }

        // Check token expiry
        if (user.getVerificationTokenExpiry() == null || Instant.now().isAfter(user.getVerificationTokenExpiry())) {
            logger.warn("Verification token expired for user: {}", user.getUsername());
            throw new RuntimeException("Verification token has expired");
        }

        // Verify email and enable account
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);

        userRepository.save(user);
        logger.info("Email successfully verified for user: {}", user.getUsername());

        // Send welcome email
        emailService.queueWelcomeEmail(user);

        return "Email verification successful";
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        logger.info("Password reset requested for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        // Generate reset token
        user.setPasswordResetToken(UUID.randomUUID().toString());
        user.setPasswordResetTokenExpiry(Instant.now().plusSeconds(60 * 60)); // 1 hour

        userRepository.save(user);

        // Send reset email
        emailService.queuePasswordResetEmail(user);

        logger.info("Password reset email sent to: {}", request.getEmail());
        return "Password reset email sent";
    }

    public String resetPassword(ResetPasswordRequest request) {
        logger.info("Attempting password reset for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        // Check token
        if (!request.getToken().equals(user.getPasswordResetToken())) {
            logger.warn("Invalid password reset token for user: {}", user.getUsername());
            throw new RuntimeException("Invalid or expired reset token");
        }

        // Check token expiry
        if (user.getPasswordResetTokenExpiry() == null || Instant.now().isAfter(user.getPasswordResetTokenExpiry())) {
            logger.warn("Password reset token expired for user: {}", user.getUsername());
            throw new RuntimeException("Invalid or expired reset token");
        }

        // Reset password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);

        userRepository.save(user);
        logger.info("Password successfully reset for user: {}", user.getUsername());

        return "Password reset successful";
    }

    public String resendVerificationEmail(String email) {
        logger.info("Resending verification email for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if (user.isEmailVerified()) {
            return "Email already verified";
        }

        // Generate new verification token
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(Instant.now().plusSeconds(24 * 60 * 60)); // 24 hours

        userRepository.save(user);

        // Send verification email
        emailService.queueVerificationEmail(user);

        logger.info("Verification email resent to: {}", email);
        return "Verification email sent";
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
