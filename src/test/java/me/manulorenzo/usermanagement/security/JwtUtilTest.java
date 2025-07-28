package me.manulorenzo.usermanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;
    private final String testSecret = "testSecretKeyThatIsLongEnoughForHS256Algorithm";
    private final long testAccessTokenExpiration = 60000; // 1 minute for testing
    private final long testRefreshTokenExpiration = 120000; // 2 minutes for testing

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(testSecret, testAccessTokenExpiration, testRefreshTokenExpiration);
        userDetails = new User(
                "testuser",
                "password",
                true, true, true, true,
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );
    }

    @Test
    void generateToken_ShouldCreateValidToken_WhenUserDetailsProvided() {
        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Token should have 3 parts separated by dots
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length);
    }

    @Test
    void generateRefreshToken_ShouldCreateValidToken_WhenUserDetailsProvided() {
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());

        // Token should have 3 parts separated by dots
        String[] tokenParts = refreshToken.split("\\.");
        assertEquals(3, tokenParts.length);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername_WhenValidToken() {
        String token = jwtUtil.generateToken(userDetails);
        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals("testuser", extractedUsername);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername_WhenValidRefreshToken() {
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        String extractedUsername = jwtUtil.extractUsername(refreshToken);

        assertEquals("testuser", extractedUsername);
    }

    @Test
    void isTokenExpired_ShouldReturnFalse_WhenTokenIsValid() {
        String token = jwtUtil.generateToken(userDetails);
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_ShouldReturnFalse_WhenRefreshTokenIsValid() {
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        assertFalse(jwtUtil.isTokenExpired(refreshToken));
    }

    @Test
    void isTokenExpired_ShouldReturnTrue_WhenTokenIsExpired() {
        // Create JwtUtil with very short expiration for testing
        JwtUtil shortExpirationJwtUtil = new JwtUtil(testSecret, 1, 1); // 1 millisecond
        String token = shortExpirationJwtUtil.generateToken(userDetails);

        // Wait a bit to ensure token expires
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue(shortExpirationJwtUtil.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_ShouldReturnTrue_WhenTokenIsMalformed() {
        String malformedToken = "malformed.token.here";
        assertTrue(jwtUtil.isTokenExpired(malformedToken));
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValidAndUsernameMatches() {
        String token = jwtUtil.generateToken(userDetails);
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenRefreshTokenIsValidAndUsernameMatches() {
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        assertTrue(jwtUtil.validateToken(refreshToken, userDetails));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        String token = jwtUtil.generateToken(userDetails);

        UserDetails differentUser = new User(
                "differentuser",
                "password",
                true, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        assertFalse(jwtUtil.validateToken(token, differentUser));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        // Create JwtUtil with very short expiration
        JwtUtil shortExpirationJwtUtil = new JwtUtil(testSecret, 1, 1);
        String token = shortExpirationJwtUtil.generateToken(userDetails);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(shortExpirationJwtUtil.validateToken(token, userDetails));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsMalformed() {
        String malformedToken = "malformed.token.here";
        assertFalse(jwtUtil.validateToken(malformedToken, userDetails));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsNull() {
        assertFalse(jwtUtil.validateToken(null, userDetails));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsEmpty() {
        assertFalse(jwtUtil.validateToken("", userDetails));
    }

    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsNull() {
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUsername(null);
        });
    }

    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsEmpty() {
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUsername("");
        });
    }

    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsMalformed() {
        String malformedToken = "malformed.token.here";

        assertThrows(Exception.class, () -> {
            jwtUtil.extractUsername(malformedToken);
        });
    }

    @Test
    void generateToken_ShouldCreateDifferentTokens_WhenCalledMultipleTimes() {
        String token1 = jwtUtil.generateToken(userDetails);

        // Wait a millisecond to ensure different issued at time
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String token2 = jwtUtil.generateToken(userDetails);

        assertNotEquals(token1, token2);
    }

    @Test
    void generateRefreshToken_ShouldCreateDifferentTokens_WhenCalledMultipleTimes() {
        String refreshToken1 = jwtUtil.generateRefreshToken(userDetails);

        // Wait a millisecond to ensure different issued at time
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String refreshToken2 = jwtUtil.generateRefreshToken(userDetails);

        assertNotEquals(refreshToken1, refreshToken2);
    }

    @Test
    void validateToken_ShouldHandleUserWithNoAuthorities() {
        UserDetails userWithNoRoles = new User(
                "noroles",
                "password",
                true, true, true, true,
                List.of() // No authorities
        );

        String token = jwtUtil.generateToken(userWithNoRoles);
        assertTrue(jwtUtil.validateToken(token, userWithNoRoles));
    }

    @Test
    void jwtUtil_ShouldWork_WithDifferentSecretKey() {
        String differentSecret = "anotherSecretKeyThatIsAlsoLongEnoughForSecurity";
        JwtUtil jwtUtilWithDifferentSecret = new JwtUtil(differentSecret, testAccessTokenExpiration, testRefreshTokenExpiration);

        String token = jwtUtilWithDifferentSecret.generateToken(userDetails);
        assertNotNull(token);
        assertTrue(jwtUtilWithDifferentSecret.validateToken(token, userDetails));

        // Token generated with different secret should not be valid with original jwtUtil
        assertFalse(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void jwtUtil_ShouldWork_WithDifferentExpirationTime() {
        long differentAccessExpiration = 300000; // 5 minutes
        long differentRefreshExpiration = 600000; // 10 minutes
        JwtUtil jwtUtilWithDifferentExpiration = new JwtUtil(testSecret, differentAccessExpiration, differentRefreshExpiration);

        String token = jwtUtilWithDifferentExpiration.generateToken(userDetails);
        String refreshToken = jwtUtilWithDifferentExpiration.generateRefreshToken(userDetails);

        assertNotNull(token);
        assertNotNull(refreshToken);
        assertTrue(jwtUtilWithDifferentExpiration.validateToken(token, userDetails));
        assertTrue(jwtUtilWithDifferentExpiration.validateToken(refreshToken, userDetails));
    }

    @Test
    void jwtUtil_ShouldUseDefaultKey_WhenDefaultSecretProvided() {
        String defaultSecret = "default-secret-key-change-in-production-and-make-it-longer-than-32-chars";
        JwtUtil jwtUtilWithDefaultSecret = new JwtUtil(defaultSecret, testAccessTokenExpiration, testRefreshTokenExpiration);

        String token = jwtUtilWithDefaultSecret.generateToken(userDetails);
        assertNotNull(token);
        assertTrue(jwtUtilWithDefaultSecret.validateToken(token, userDetails));
    }

    @Test
    void extractUsername_ShouldWorkWithDifferentUsernames() {
        UserDetails user1 = new User("user1", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetails user2 = new User("user@example.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetails user3 = new User("user_with_underscores", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);
        String token3 = jwtUtil.generateToken(user3);

        assertEquals("user1", jwtUtil.extractUsername(token1));
        assertEquals("user@example.com", jwtUtil.extractUsername(token2));
        assertEquals("user_with_underscores", jwtUtil.extractUsername(token3));
    }

    @Test
    void validateToken_ShouldWorkForComplexUsernames() {
        UserDetails complexUser = new User(
                "complex.user+test@example.com",
                "password",
                true, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtUtil.generateToken(complexUser);
        assertTrue(jwtUtil.validateToken(token, complexUser));
        assertEquals("complex.user+test@example.com", jwtUtil.extractUsername(token));
    }
}