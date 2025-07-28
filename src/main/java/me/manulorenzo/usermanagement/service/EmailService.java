package me.manulorenzo.usermanagement.service;

import me.manulorenzo.usermanagement.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String baseUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.email.from:noreply@usermanagement.com}") String fromEmail,
            @Value("${app.base-url:http://localhost:8082}") String baseUrl) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.baseUrl = baseUrl;
        logger.info("EmailService initialized with from: '{}', baseUrl: '{}'", fromEmail, baseUrl);
    }

    public void sendVerificationEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Verify Your Email - User Management");

            String verificationUrl = baseUrl + "/verify-email?email=" + user.getEmail() + "&token=" + user.getVerificationToken();

            message.setText(String.format(
                    "Hi %s,\n\n" +
                            "Thank you for registering with User Management!\n\n" +
                            "Please click the following link to verify your email address:\n" +
                            "%s\n\n" +
                            "Or use this verification code: %s\n\n" +
                            "This link will expire in 24 hours.\n\n" +
                            "If you didn't register for an account, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "User Management Team",
                    user.getUsername(),
                    verificationUrl,
                    user.getVerificationToken()
            ));

            mailSender.send(message);
            logger.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}", user.getEmail(), e);
            // In development, log the token so you can test manually
            logger.warn("Development - Verification token for {}: {}", user.getEmail(), user.getVerificationToken());
        }
    }

    public void sendPasswordResetEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Password Reset - User Management");

            String resetUrl = baseUrl + "/reset-password?email=" + user.getEmail() + "&token=" + user.getPasswordResetToken();

            message.setText(String.format(
                    "Hi %s,\n\n" +
                            "You requested a password reset for your User Management account.\n\n" +
                            "Please click the following link to reset your password:\n" +
                            "%s\n\n" +
                            "Or use this reset code: %s\n\n" +
                            "This link will expire in 1 hour.\n\n" +
                            "If you didn't request a password reset, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "User Management Team",
                    user.getUsername(),
                    resetUrl,
                    user.getPasswordResetToken()
            ));

            mailSender.send(message);
            logger.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", user.getEmail(), e);
            // In development, log the token so you can test manually
            logger.warn("Development - Password reset token for {}: {}", user.getEmail(), user.getPasswordResetToken());
        }
    }

    public void sendWelcomeEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Welcome to User Management!");

            message.setText(String.format(
                    "Hi %s,\n\n" +
                            "Welcome to User Management! Your email has been successfully verified.\n\n" +
                            "You can now log in and use all features of the application.\n\n" +
                            "Login at: %s/swagger-ui.html\n\n" +
                            "Your role: %s\n\n" +
                            "Best regards,\n" +
                            "User Management Team",
                    user.getUsername(),
                    baseUrl,
                    user.getRoles().isEmpty() ? "USER" : user.getRoles().iterator().next().getName()
            ));

            mailSender.send(message);
            logger.info("Welcome email sent to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }
}