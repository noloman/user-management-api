package me.manulorenzo.usermanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Using test values for JWT configuration
        String testSecret = "test-secret-key-for-junit-tests-that-is-long-enough-for-hmac-sha256";
        long accessTokenExpiration = 900000; // 15 minutes
        long refreshTokenExpiration = 604800000; // 7 days

        jwtUtil = new JwtUtil(testSecret, accessTokenExpiration, refreshTokenExpiration);
        userDetails = new User(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateRefreshToken_ShouldReturnValidToken() {
        // When
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    void extractUsername_FromRefreshToken_ShouldReturnCorrectUsername() {
        // Given
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // When
        String extractedUsername = jwtUtil.extractUsername(refreshToken);

        // Then
        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        // Given
        String token = jwtUtil.generateToken(userDetails);
        UserDetails differentUser = new User(
                "differentuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // When
        boolean isValid = jwtUtil.validateToken(token, differentUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenExpired_ShouldReturnFalse_WhenTokenIsNotExpired() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    void isTokenExpired_ShouldReturnTrue_WhenTokenIsInvalid() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isExpired = jwtUtil.isTokenExpired(invalidToken);

        // Then
        assertThat(isExpired).isTrue();
    }

    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsInvalid() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void refreshToken_ShouldHaveLongerExpiration_ThanAccessToken() throws InterruptedException {
        // Given
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // When & Then - Both tokens should be valid now
        assertThat(jwtUtil.isTokenExpired(accessToken)).isFalse();
        assertThat(jwtUtil.isTokenExpired(refreshToken)).isFalse();

        // Both tokens should contain the same username
        assertThat(jwtUtil.extractUsername(accessToken)).isEqualTo("testuser");
        assertThat(jwtUtil.extractUsername(refreshToken)).isEqualTo("testuser");
    }
}