package me.manulorenzo.usermanagement.repository;

import me.manulorenzo.usermanagement.entity.RefreshToken;
import me.manulorenzo.usermanagement.entity.Role;
import me.manulorenzo.usermanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        // Create and persist role
        Role userRole = new Role();
        userRole.setName("USER");
        userRole = entityManager.persistAndFlush(userRole);

        // Create and persist user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);
        testUser = entityManager.persistAndFlush(testUser);

        // Create refresh token
        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken("test-refresh-token");
        testRefreshToken.setUser(testUser);
        testRefreshToken.setExpiryDate(Instant.now().plusSeconds(86400));
    }

    @Test
    void findByToken_ShouldReturnToken_WhenTokenExists() {
        // Given
        testRefreshToken = entityManager.persistAndFlush(testRefreshToken);

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("test-refresh-token");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("test-refresh-token");
        assertThat(result.get().getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByToken_ShouldReturnEmpty_WhenTokenDoesNotExist() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("nonexistent-token");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUser_ShouldReturnToken_WhenUserHasToken() {
        // Given
        testRefreshToken = entityManager.persistAndFlush(testRefreshToken);

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByUser(testUser);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getToken()).isEqualTo("test-refresh-token");
    }

    @Test
    void findByUser_ShouldReturnEmpty_WhenUserHasNoToken() {
        // Given
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        anotherUser = entityManager.persistAndFlush(anotherUser);

        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByUser(anotherUser);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void deleteByUser_ShouldDeleteToken_WhenUserHasToken() {
        // Given
        testRefreshToken = entityManager.persistAndFlush(testRefreshToken);

        // Verify token exists
        assertThat(refreshTokenRepository.findByUser(testUser)).isPresent();

        // When
        refreshTokenRepository.deleteByUser(testUser);
        entityManager.flush();

        // Then
        assertThat(refreshTokenRepository.findByUser(testUser)).isEmpty();
    }

    @Test
    void deleteByUser_ShouldNotThrow_WhenUserHasNoToken() {
        // Given
        User userWithoutToken = new User();
        userWithoutToken.setUsername("notoken");
        userWithoutToken.setEmail("notoken@example.com");
        userWithoutToken.setPassword("password");
        userWithoutToken = entityManager.persistAndFlush(userWithoutToken);

        // When & Then - should not throw
        refreshTokenRepository.deleteByUser(userWithoutToken);
        entityManager.flush();
    }

    @Test
    void save_ShouldPersistToken() {
        // When
        RefreshToken savedToken = refreshTokenRepository.save(testRefreshToken);

        // Then
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo("test-refresh-token");
        assertThat(savedToken.getUser()).isEqualTo(testUser);
        assertThat(savedToken.getExpiryDate()).isNotNull();
    }

    @Test
    void delete_ShouldRemoveToken() {
        // Given
        testRefreshToken = entityManager.persistAndFlush(testRefreshToken);
        Long tokenId = testRefreshToken.getId();

        // When
        refreshTokenRepository.delete(testRefreshToken);
        entityManager.flush();

        // Then
        assertThat(refreshTokenRepository.findById(tokenId)).isEmpty();
    }
}