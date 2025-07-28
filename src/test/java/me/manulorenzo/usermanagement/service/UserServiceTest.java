package me.manulorenzo.usermanagement.service;

import me.manulorenzo.usermanagement.dto.*;
import me.manulorenzo.usermanagement.entity.Role;
import me.manulorenzo.usermanagement.entity.User;
import me.manulorenzo.usermanagement.repository.RoleRepository;
import me.manulorenzo.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private RoleRepository roleRepo;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private EmailService emailService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        // Initialize UserService with test configuration values
        String adminRoleName = "ADMIN";
        String userRoleName = "USER";
        boolean firstUserAdmin = true;

        userService = new UserService(
                adminRoleName,
                userRoleName,
                firstUserAdmin,
                userRepo,
                roleRepo,
                encoder,
                emailService
        );
    }

    // Registration Tests
    @Test
    void register_ShouldSaveUser_WhenValidRequest() {
        RegisterRequest request = new RegisterRequest("john", "john@example.com", "password");
        when(userRepo.findByUsername("john")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("password")).thenReturn("hashed");
        when(userRepo.count()).thenReturn(0L);
        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.of(new Role("ADMIN")));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        verify(userRepo).save(any(User.class));
        verify(emailService).sendVerificationEmail(any(User.class));
    }

    @Test
    void register_ShouldSaveFirstUser_WithAdminRole() {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "pass");
        when(userRepo.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("pass")).thenReturn("hashed_pass");
        when(userRepo.count()).thenReturn(0L);
        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.of(new Role("ADMIN")));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);
        verify(userRepo).save(any(User.class));
        verify(emailService).sendVerificationEmail(any(User.class));
    }

    @Test
    void register_ShouldSaveUser_WhenNotFirstUser_UserRole() {
        RegisterRequest request = new RegisterRequest("bob", "bob@example.com", "secret");
        when(userRepo.findByUsername("bob")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("bob@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("secret")).thenReturn("enc_secret");
        when(userRepo.count()).thenReturn(10L);
        when(roleRepo.findByName("USER")).thenReturn(Optional.of(new Role("USER")));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);
        verify(userRepo).save(any(User.class));
        verify(emailService).sendVerificationEmail(any(User.class));
    }

    @Test
    void register_ShouldThrow_WhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("existing", "existing@example.com", "pass");
        when(userRepo.findByUsername("existing")).thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> userService.register(request));
        verify(userRepo, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(any(User.class));
    }

    @Test
    void register_ShouldThrow_WhenEmailExists() {
        RegisterRequest request = new RegisterRequest("newuser", "existing@example.com", "pass");
        when(userRepo.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> userService.register(request));
        verify(userRepo, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(any(User.class));
    }

    @Test
    void register_ShouldThrow_WhenRoleMissing() {
        RegisterRequest request = new RegisterRequest("brad", "brad@example.com", "foo");
        when(userRepo.findByUsername("brad")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("brad@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("foo")).thenReturn("enc_foo");
        when(userRepo.count()).thenReturn(1L);
        when(roleRepo.findByName("USER")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.register(request));
        verify(userRepo, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(any(User.class));
    }

    // Email Verification Tests
    @Test
    void verifyEmail_ShouldActivateUser_WhenValidToken() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("john@example.com");
        request.setToken("valid-token");

        User user = new User();
        user.setEmail("john@example.com");
        user.setVerificationToken("valid-token");
        user.setVerificationTokenExpiry(Instant.now().plusSeconds(3600));
        user.setEnabled(false);
        user.setEmailVerified(false);

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        String result = userService.verifyEmail(request);

        assertEquals("Email verification successful", result);
        assertTrue(user.isEnabled());
        assertTrue(user.isEmailVerified());
        assertNull(user.getVerificationToken());
        assertNull(user.getVerificationTokenExpiry());
        verify(userRepo).save(user);
        verify(emailService).sendWelcomeEmail(user);
    }

    @Test
    void verifyEmail_ShouldThrow_WhenUserNotFound() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("nonexistent@example.com");
        request.setToken("some-token");

        when(userRepo.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.verifyEmail(request));
        verify(userRepo, never()).save(any(User.class));
        verify(emailService, never()).sendWelcomeEmail(any(User.class));
    }

    @Test
    void verifyEmail_ShouldThrow_WhenTokenMismatch() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("john@example.com");
        request.setToken("wrong-token");

        User user = new User();
        user.setEmail("john@example.com");
        user.setVerificationToken("correct-token");
        user.setVerificationTokenExpiry(Instant.now().plusSeconds(3600));

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.verifyEmail(request));
        verify(userRepo, never()).save(any(User.class));
        verify(emailService, never()).sendWelcomeEmail(any(User.class));
    }

    @Test
    void verifyEmail_ShouldThrow_WhenTokenExpired() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("john@example.com");
        request.setToken("expired-token");

        User user = new User();
        user.setEmail("john@example.com");
        user.setVerificationToken("expired-token");
        user.setVerificationTokenExpiry(Instant.now().minusSeconds(3600)); // expired

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.verifyEmail(request));
        verify(userRepo, never()).save(any(User.class));
        verify(emailService, never()).sendWelcomeEmail(any(User.class));
    }

    @Test
    void resendVerificationEmail_ShouldSendEmail_WhenUserExists() {
        String email = "john@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEmailVerified(false);

        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        String result = userService.resendVerificationEmail(email);

        assertEquals("Verification email sent", result);
        assertNotNull(user.getVerificationToken());
        assertNotNull(user.getVerificationTokenExpiry());
        verify(userRepo).save(user);
        verify(emailService).sendVerificationEmail(user);
    }

    @Test
    void resendVerificationEmail_ShouldThrow_WhenUserNotFound() {
        String email = "nonexistent@example.com";
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.resendVerificationEmail(email));
        verify(userRepo, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(any(User.class));
    }

    @Test
    void resendVerificationEmail_ShouldThrow_WhenEmailAlreadyVerified() {
        String email = "verified@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEmailVerified(true);

        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        String result = userService.resendVerificationEmail(email);

        assertEquals("Email already verified", result);
        verify(userRepo, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(any(User.class));
    }

    // Password Reset Tests
    @Test
    void forgotPassword_ShouldSendResetEmail_WhenUserExists() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("john@example.com");

        User user = new User();
        user.setEmail("john@example.com");

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        String result = userService.forgotPassword(request);

        assertEquals("Password reset email sent", result);
        assertNotNull(user.getPasswordResetToken());
        assertNotNull(user.getPasswordResetTokenExpiry());
        verify(userRepo).save(user);
        verify(emailService).sendPasswordResetEmail(user);
    }

    @Test
    void forgotPassword_ShouldThrow_WhenUserNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nonexistent@example.com");

        when(userRepo.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.forgotPassword(request));
        verify(userRepo, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordResetEmail(any(User.class));
    }

    @Test
    void resetPassword_ShouldUpdatePassword_WhenValidToken() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("john@example.com");
        request.setToken("valid-reset-token");
        request.setNewPassword("newPassword123");

        User user = new User();
        user.setEmail("john@example.com");
        user.setPasswordResetToken("valid-reset-token");
        user.setPasswordResetTokenExpiry(Instant.now().plusSeconds(3600));

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(encoder.encode("newPassword123")).thenReturn("encoded-new-password");

        String result = userService.resetPassword(request);

        assertEquals("Password reset successful", result);
        assertEquals("encoded-new-password", user.getPassword());
        assertNull(user.getPasswordResetToken());
        assertNull(user.getPasswordResetTokenExpiry());
        verify(userRepo).save(user);
    }

    @Test
    void resetPassword_ShouldThrow_WhenUserNotFound() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("nonexistent@example.com");
        request.setToken("some-token");
        request.setNewPassword("newPassword123");

        when(userRepo.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.resetPassword(request));
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void resetPassword_ShouldThrow_WhenTokenMismatch() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("john@example.com");
        request.setToken("wrong-token");
        request.setNewPassword("newPassword123");

        User user = new User();
        user.setEmail("john@example.com");
        user.setPasswordResetToken("correct-token");
        user.setPasswordResetTokenExpiry(Instant.now().plusSeconds(3600));

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.resetPassword(request));
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void resetPassword_ShouldThrow_WhenTokenExpired() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("john@example.com");
        request.setToken("expired-token");
        request.setNewPassword("newPassword123");

        User user = new User();
        user.setEmail("john@example.com");
        user.setPasswordResetToken("expired-token");
        user.setPasswordResetTokenExpiry(Instant.now().minusSeconds(3600)); // expired

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.resetPassword(request));
        verify(userRepo, never()).save(any(User.class));
    }

    // Role Management Tests
    @Test
    void addRoleToUser_ShouldAddRole_WhenUserAndRoleExist() {
        User user = new User();
        user.setUsername("joan");
        Role role = new Role("MODERATOR");
        when(userRepo.findByUsername("joan")).thenReturn(Optional.of(user));
        when(roleRepo.findByName("MODERATOR")).thenReturn(Optional.of(role));

        userService.addRoleToUser("joan", "MODERATOR");
        verify(userRepo).save(user);
        assertTrue(user.getRoles().contains(role));
    }

    @Test
    void addRoleToUser_ShouldThrow_WhenUserNotFound() {
        when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.addRoleToUser("ghost", "SOME_ROLE"));
    }

    @Test
    void addRoleToUser_ShouldThrow_WhenRoleNotFound() {
        User user = new User();
        user.setUsername("pete");
        when(userRepo.findByUsername("pete")).thenReturn(Optional.of(user));
        when(roleRepo.findByName("MISSING")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.addRoleToUser("pete", "MISSING"));
    }

    @Test
    void addRoleToUser_ShouldNotDuplicateRole_WhenUserAlreadyHasRole() {
        User user = new User();
        user.setUsername("jane");
        Role role = new Role("USER");
        user.getRoles().add(role); // user already has the role

        when(userRepo.findByUsername("jane")).thenReturn(Optional.of(user));
        when(roleRepo.findByName("USER")).thenReturn(Optional.of(role));

        userService.addRoleToUser("jane", "USER");

        verify(userRepo, never()).save(user); // Should not save if role already exists
        assertEquals(1, user.getRoles().size()); // Should still have only one role
    }

    // Profile Management Tests
    @Test
    void getUserProfile_ShouldReturnProfile_WhenUserExists() {
        User user = new User();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setFullName("John Doe");
        user.setBio("Developer");
        user.setImageUrl("http://example.com/image.jpg");

        when(userRepo.findByUsername("john")).thenReturn(Optional.of(user));

        UserProfile profile = userService.getUserProfile("john");

        assertEquals("john", profile.getUsername());
        assertEquals("john@example.com", profile.getEmail());
        assertEquals("John Doe", profile.getFullName());
        assertEquals("Developer", profile.getBio());
        assertEquals("http://example.com/image.jpg", profile.getImageUrl());
    }

    @Test
    void getUserProfile_ShouldThrow_WhenUserNotFound() {
        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.getUserProfile("nonexistent"));
    }

    @Test
    void updateUserProfile_ShouldThrow_WhenChangingToExistingUsername() {
        User oldUser = new User();
        oldUser.setUsername("oldUser");
        oldUser.setEmail("old@example.com");
        when(userRepo.findByUsername("oldUser")).thenReturn(Optional.of(oldUser));
        when(userRepo.findByUsername("newName")).thenReturn(Optional.of(new User())); // already taken

        UserProfile changes = new UserProfile();
        changes.setUsername("newName"); // try changing to taken username
        changes.setEmail("other@example.com");

        assertThrows(RuntimeException.class, () -> userService.updateUserProfile("oldUser", changes));
    }

    @Test
    void updateUserProfile_ShouldThrow_WhenChangingToExistingEmail() {
        User oldUser = new User();
        oldUser.setUsername("carl");
        oldUser.setEmail("carl@ex.com");
        when(userRepo.findByUsername("carl")).thenReturn(Optional.of(oldUser));
        when(userRepo.findByEmail("dupe@ex.com")).thenReturn(Optional.of(new User())); // already taken

        UserProfile changes = new UserProfile();
        changes.setUsername("carl");
        changes.setEmail("dupe@ex.com"); // try changing to taken email

        assertThrows(RuntimeException.class, () -> userService.updateUserProfile("carl", changes));
    }

    @Test
    void updateUserProfile_ShouldUpdate_WhenUsernameAndEmailUnique() {
        User oldUser = new User();
        oldUser.setUsername("jdoe");
        oldUser.setEmail("jdoe@ex.com");
        when(userRepo.findByUsername("jdoe")).thenReturn(Optional.of(oldUser));
        when(userRepo.findByUsername("newjdoe")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("new@ex.com")).thenReturn(Optional.empty());

        UserProfile changes = new UserProfile();
        changes.setUsername("newjdoe");
        changes.setEmail("new@ex.com");
        changes.setFullName("Joe Doe");
        changes.setBio("Bio");
        changes.setImageUrl("A");

        UserProfile result = userService.updateUserProfile("jdoe", changes);

        verify(userRepo).save(oldUser);
        assertEquals("newjdoe", oldUser.getUsername());
        assertEquals("new@ex.com", oldUser.getEmail());
        assertEquals("Joe Doe", oldUser.getFullName());
        assertEquals("newjdoe", result.getUsername());
    }

    @Test
    void updateUserProfile_ShouldUpdateFields_WhenNoUsernameOrEmailChange() {
        User oldUser = new User();
        oldUser.setUsername("emma");
        oldUser.setEmail("emma@ex.com");
        when(userRepo.findByUsername("emma")).thenReturn(Optional.of(oldUser));

        UserProfile changes = new UserProfile();
        changes.setUsername("emma");
        changes.setEmail("emma@ex.com");
        changes.setFullName("Emma Watson");
        changes.setBio("Actress");
        changes.setImageUrl("img");

        UserProfile result = userService.updateUserProfile("emma", changes);

        verify(userRepo).save(oldUser);
        assertEquals("emma", oldUser.getUsername());
        assertEquals("Emma Watson", oldUser.getFullName());
        assertEquals("Actress", oldUser.getBio());
        assertEquals("Emma Watson", result.getFullName());
    }

    @Test
    void updateUserProfile_ShouldThrow_WhenUserNotFound() {
        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UserProfile changes = new UserProfile();
        changes.setUsername("nonexistent");

        assertThrows(UsernameNotFoundException.class, () ->
                userService.updateUserProfile("nonexistent", changes));
    }
}
