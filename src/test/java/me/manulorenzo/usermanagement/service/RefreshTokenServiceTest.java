package me.manulorenzo.usermanagement.service;

import me.manulorenzo.usermanagement.entity.RefreshToken;
import me.manulorenzo.usermanagement.entity.Role;
import me.manulorenzo.usermanagement.entity.User;
import me.manulorenzo.usermanagement.repository.RefreshTokenRepository;
import me.manulorenzo.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    private RefreshTokenService refreshTokenService;

    private User testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        // Initialize RefreshTokenService with test configuration values
        long refreshTokenExpirationMs = 604800000L; // 7 days in milliseconds

        refreshTokenService = new RefreshTokenService(
                refreshTokenExpirationMs,
                refreshTokenRepository,
                userRepository
        );

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");

        Role userRole = new Role();
        userRole.setName("USER");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);

        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken("test-refresh-token");
        testRefreshToken.setUser(testUser);
        testRefreshToken.setExpiryDate(Instant.now().plusSeconds(86400)); // 1 day from now
    }

    @Test
    void createRefreshToken_ShouldCreateNewToken_WhenUserExists() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        // When
        RefreshToken result = refreshTokenService.createRefreshToken("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getExpiryDate()).isAfter(Instant.now());

        verify(userRepository).findByUsername("testuser");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_ShouldDeleteExistingToken_WhenUserHasExistingToken() {
        // Given
        RefreshToken existingToken = new RefreshToken();
        existingToken.setToken("existing-token");
        existingToken.setUser(testUser);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.findByUser(testUser)).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        // When
        RefreshToken result = refreshTokenService.createRefreshToken("testuser");

        // Then
        assertThat(result).isNotNull();
        verify(refreshTokenRepository).delete(existingToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> refreshTokenService.createRefreshToken("nonexistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found: nonexistent");

        verify(userRepository).findByUsername("nonexistent");
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void findByToken_ShouldReturnToken_WhenTokenExists() {
        // Given
        when(refreshTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testRefreshToken));

        // When
        Optional<RefreshToken> result = refreshTokenService.findByToken("test-token");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRefreshToken);

        verify(refreshTokenRepository).findByToken("test-token");
    }

    @Test
    void findByToken_ShouldReturnEmpty_WhenTokenNotExists() {
        // Given
        when(refreshTokenRepository.findByToken("nonexistent-token")).thenReturn(Optional.empty());

        // When
        Optional<RefreshToken> result = refreshTokenService.findByToken("nonexistent-token");

        // Then
        assertThat(result).isEmpty();

        verify(refreshTokenRepository).findByToken("nonexistent-token");
    }

    @Test
    void verifyExpiration_ShouldReturnToken_WhenTokenNotExpired() {
        // Given
        testRefreshToken.setExpiryDate(Instant.now().plusSeconds(3600)); // 1 hour from now

        // When
        RefreshToken result = refreshTokenService.verifyExpiration(testRefreshToken);

        // Then
        assertThat(result).isEqualTo(testRefreshToken);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_ShouldThrowException_WhenTokenExpired() {
        // Given
        testRefreshToken.setExpiryDate(Instant.now().minusSeconds(3600)); // 1 hour ago

        // When & Then
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(testRefreshToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Refresh token was expired. Please make a new signin request");

        verify(refreshTokenRepository).delete(testRefreshToken);
    }

    @Test
    void deleteByUser_ShouldCallRepositoryDelete() {
        // When
        refreshTokenService.deleteByUser(testUser);

        // Then
        verify(refreshTokenRepository).deleteByUser(testUser);
    }

    @Test
    void deleteByToken_ShouldDeleteToken_WhenTokenExists() {
        // Given
        when(refreshTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testRefreshToken));

        // When
        refreshTokenService.deleteByToken("test-token");

        // Then
        verify(refreshTokenRepository).findByToken("test-token");
        verify(refreshTokenRepository).delete(testRefreshToken);
    }

    @Test
    void deleteByToken_ShouldNotDelete_WhenTokenNotExists() {
        // Given
        when(refreshTokenRepository.findByToken("nonexistent-token")).thenReturn(Optional.empty());

        // When
        refreshTokenService.deleteByToken("nonexistent-token");

        // Then
        verify(refreshTokenRepository).findByToken("nonexistent-token");
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }
}