package me.manulorenzo.usermanagement.service;

import me.manulorenzo.usermanagement.entity.RefreshToken;
import me.manulorenzo.usermanagement.entity.User;
import me.manulorenzo.usermanagement.repository.RefreshTokenRepository;
import me.manulorenzo.usermanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    private final long refreshTokenExpirationMs;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(
            @Value("${jwt.refresh-token.expiration-ms}") long refreshTokenExpirationMs,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository) {
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;

        logger.info("RefreshTokenService initialized with expiration: {}ms", refreshTokenExpirationMs);
    }

    public RefreshToken createRefreshToken(String username) {
        logger.debug("Creating refresh token for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Delete existing refresh token for this user
        refreshTokenRepository.findByUser(user).ifPresent(existingToken -> {
            logger.debug("Deleting existing refresh token for user: {}", username);
            refreshTokenRepository.delete(existingToken);
        });

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpirationMs));

        refreshToken = refreshTokenRepository.save(refreshToken);

        logger.info("Refresh token created successfully for user: {}", username);
        return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            logger.warn("Refresh token expired for user: {}", token.getUser().getUsername());
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUser(User user) {
        logger.debug("Deleting refresh token for user: {}", user.getUsername());
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void deleteByToken(String token) {
        logger.debug("Deleting refresh token: {}", token);
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }
}