package me.manulorenzo.usermanagement.service;

import me.manulorenzo.usermanagement.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final RabbitTemplate rabbitTemplate;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.email.from:noreply@usermanagement.com}") String fromEmail,
            @Value("${app.base-url:http://localhost:8082}") String baseUrl,
            RabbitTemplate rabbitTemplate) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.baseUrl = baseUrl;
        this.rabbitTemplate = rabbitTemplate;
        logger.info("EmailService initialized with from: '{}', baseUrl: '{}'", fromEmail, baseUrl);
    }

    public void sendVerificationEmail(EmailTask task) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(task.getEmail());
            message.setSubject("Verify Your Email - User Management");

            String verificationUrl = baseUrl + "/verify-email?email=" + task.getEmail() + "&token=" + task.getToken();

            message.setText(String.format(
                    """
                            Hi %s,
                            
                            Thank you for registering with User Management!
                            
                            Please click the following link to verify your email address:
                            %s
                            
                            Or use this verification code: %s
                            
                            This link will expire in 24 hours.
                            
                            If you didn't register for an account, please ignore this email.
                            
                            Best regards,
                            User Management Team""",
                    task.getUsername(),
                    verificationUrl,
                    task.getToken()
            ));

            mailSender.send(message);
            logger.info("Verification email sent to: {}", task.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}", task.getEmail(), e);
            // In development, log the token so you can test manually
            logger.warn("Development - Verification token for {}: {}", task.getEmail(), task.getToken());
        }
    }

    public void sendPasswordResetEmail(EmailTask task) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(task.getEmail());
            message.setSubject("Password Reset - User Management");

            String resetUrl = baseUrl + "/reset-password?email=" + task.getEmail() + "&token=" + task.getToken();

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
                    task.getUsername(),
                    resetUrl,
                    task.getToken()
            ));

            mailSender.send(message);
            logger.info("Password reset email sent to: {}", task.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", task.getEmail(), e);
            // In development, log the token so you can test manually
            logger.warn("Development - Password reset token for {}: {}", task.getEmail(), task.getToken());
        }
    }

    public void sendWelcomeEmail(EmailTask task) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(task.getEmail());
            message.setSubject("Welcome to User Management!");

            message.setText(String.format(
                    "Hi %s,\n\n" +
                            "Welcome to User Management! Your email has been successfully verified.\n\n" +
                            "You can now log in and use all features of the application.\n\n" +
                            "Login at: %s/swagger-ui.html\n\n" +
                            "Your role: %s\n\n" +
                            "Best regards,\n" +
                            "User Management Team",
                    task.getUsername(),
                    baseUrl,
                    task.getRoles()
            ));

            mailSender.send(message);
            logger.info("Welcome email sent to: {}", task.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", task.getEmail(), e);
        }
    }

    public void queueVerificationEmail(User user) {
        logger.info("Queueing verification email for user: {}", user.getEmail());
        EmailTask task = new EmailTask("verification", user.getEmail(), user.getUsername(), user.getVerificationToken());
        rabbitTemplate.convertAndSend("emailQueue", task);
    }

    public void queuePasswordResetEmail(User user) {
        logger.info("Queueing password reset email for user: {}", user.getEmail());
        EmailTask task = new EmailTask("reset", user.getEmail(), user.getUsername(), user.getPasswordResetToken(), true);
        rabbitTemplate.convertAndSend("emailQueue", task);
    }

    public void queueWelcomeEmail(User user) {
        logger.info("Queueing welcome email for user: {}", user.getEmail());
        String roles = user.getRoles() == null || user.getRoles().isEmpty() ? "USER" : user.getRoles().iterator().next().getName();
        EmailTask task = new EmailTask("welcome", user.getEmail(), user.getUsername(), roles);
        rabbitTemplate.convertAndSend("emailQueue", task);
    }
}