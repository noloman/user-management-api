package me.manulorenzo.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.manulorenzo.usermanagement.dto.*;
import me.manulorenzo.usermanagement.entity.RefreshToken;
import me.manulorenzo.usermanagement.entity.User;
import me.manulorenzo.usermanagement.security.JwtUtil;
import me.manulorenzo.usermanagement.service.RefreshTokenService;
import me.manulorenzo.usermanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_ShouldReturnOk_WhenValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("john", "john@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully. Please check your email to verify your account."));

        verify(userService).register(request);
    }

    @Test
    void register_ShouldReturnBadRequest_WhenUserServiceThrowsUsernameExists() throws Exception {
        RegisterRequest request = new RegisterRequest("existing", "existing@example.com", "password123");
        doThrow(new RuntimeException("Username already exists")).when(userService).register(request);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Registration failed: Username already exists"));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenUserServiceThrowsEmailExists() throws Exception {
        RegisterRequest request = new RegisterRequest("john", "existing@example.com", "password123");
        doThrow(new RuntimeException("Email already exists")).when(userService).register(request);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Registration failed: Email already exists"));
    }

    @Test
    void login_ShouldReturnJwt_WhenValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("secret");

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "alice", "secret", true, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        User testUser = new User();
        testUser.setUsername("alice");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-uuid");
        refreshToken.setUser(testUser);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(86400));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken("alice")).thenReturn(refreshToken);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-uuid"));
    }

    @Test
    void login_ShouldReturnBadRequest_WhenInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Login failed: Bad credentials"));
    }

    @Test
    void login_ShouldReturnBadRequest_WhenAccountDisabled() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("disabled");
        request.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User account is disabled"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Login failed: User account is disabled"));
    }

    @Test
    void refreshToken_ShouldReturnNewToken_WhenValidRefreshToken() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        User testUser = new User();
        testUser.setUsername("alice");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("valid-refresh-token");
        refreshToken.setUser(testUser);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(86400));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "alice", "password", true, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(refreshTokenService.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("new-jwt-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-jwt-token"));
    }

    @Test
    void refreshToken_ShouldReturnBadRequest_WhenInvalidRefreshToken() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-refresh-token");

        when(refreshTokenService.findByToken("invalid-refresh-token")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Token refresh failed: Refresh token is not in database!"));
    }

    @Test
    void logout_ShouldReturnOk_WhenValidRefreshToken() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));

        verify(refreshTokenService).deleteByToken("valid-refresh-token");
    }

    @Test
    void logout_ShouldReturnBadRequest_WhenRefreshTokenServiceThrows() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-refresh-token");

        doThrow(new RuntimeException("Database error")).when(refreshTokenService).deleteByToken("invalid-refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Logout failed: Database error"));
    }

    @Test
    void verifyEmail_ShouldReturnOk_WhenValidToken() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("john@example.com");
        request.setToken("valid-verification-token");

        when(userService.verifyEmail(request)).thenReturn("Email verification successful");

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email verification successful"));

        verify(userService).verifyEmail(request);
    }

    @Test
    void verifyEmail_ShouldReturnBadRequest_WhenInvalidToken() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("john@example.com");
        request.setToken("invalid-token");

        doThrow(new RuntimeException("Invalid verification token")).when(userService).verifyEmail(request);

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email verification failed: Invalid verification token"));
    }

    @Test
    void resendVerificationEmail_ShouldReturnOk_WhenValidEmail() throws Exception {
        String email = "john@example.com";

        when(userService.resendVerificationEmail(email)).thenReturn("Verification email sent");

        mockMvc.perform(post("/api/auth/resend-verification")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification email sent"));

        verify(userService).resendVerificationEmail(email);
    }

    @Test
    void resendVerificationEmail_ShouldReturnBadRequest_WhenEmailNotFound() throws Exception {
        String email = "nonexistent@example.com";

        doThrow(new RuntimeException("Email not found")).when(userService).resendVerificationEmail(email);

        mockMvc.perform(post("/api/auth/resend-verification")
                        .param("email", email))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to resend verification email: Email not found"));
    }

    @Test
    void forgotPassword_ShouldReturnOk_WhenValidEmail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("john@example.com");

        when(userService.forgotPassword(request)).thenReturn("Password reset email sent");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset email sent"));

        verify(userService).forgotPassword(request);
    }

    @Test
    void forgotPassword_ShouldReturnBadRequest_WhenEmailNotFound() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nonexistent@example.com");

        doThrow(new RuntimeException("Email not found")).when(userService).forgotPassword(request);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Forgot password failed: Email not found"));
    }

    @Test
    void resetPassword_ShouldReturnOk_WhenValidToken() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("john@example.com");
        request.setToken("valid-reset-token");
        request.setNewPassword("newPassword123");

        when(userService.resetPassword(request)).thenReturn("Password reset successful");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successful"));

        verify(userService).resetPassword(request);
    }

    @Test
    void resetPassword_ShouldReturnBadRequest_WhenInvalidToken() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("john@example.com");
        request.setToken("invalid-token");
        request.setNewPassword("newPassword123");

        doThrow(new RuntimeException("Invalid or expired reset token")).when(userService).resetPassword(request);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password reset failed: Invalid or expired reset token"));
    }
}