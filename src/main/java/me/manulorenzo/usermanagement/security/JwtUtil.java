package me.manulorenzo.usermanagement.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.access-token.expiration-ms}") long accessTokenExpiration,
            @Value("${jwt.refresh-token.expiration-ms}") long refreshTokenExpiration) {
        // Create key from configured secret or generate new one if default
        if ("default-secret-key-change-in-production-and-make-it-longer-than-32-chars".equals(jwtSecret)) {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            logger.warn("Using generated JWT secret key. Configure jwt.secret property for production!");
        } else {
            this.key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
            logger.info("Using configured JWT secret key");
        }

        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;

        logger.info("JwtUtil initialized with access token expiration: {}ms, refresh token expiration: {}ms",
                accessTokenExpiration, refreshTokenExpiration);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshTokenExpiration);
    }

    private String generateToken(UserDetails userDetails, long expiration) {
        logger.debug("Generating JWT token for user: {}", userDetails.getUsername());

        try {
            String token = Jwts.builder()
                    .setSubject(userDetails.getUsername())
                    .claim("roles", userDetails.getAuthorities())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(key)
                    .compact();

            logger.info("JWT token generated successfully for user: {}", userDetails.getUsername());
            return token;

        } catch (Exception e) {
            logger.error("Error generating JWT token for user: {}", userDetails.getUsername(), e);
            throw new RuntimeException("Token generation failed", e);
        }
    }

    public String extractUsername(String token) {
        logger.debug("Extracting username from JWT token");

        try {
            String username = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            logger.debug("Username extracted from token: {}", username);
            return username;

        } catch (Exception e) {
            logger.error("Error extracting username from JWT token: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            return expiration.before(new Date());
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        logger.debug("Validating JWT token for user: {}", userDetails.getUsername());

        try {
            String extractedUsername = extractUsername(token);
            boolean isValid = extractedUsername.equals(userDetails.getUsername()) && !isTokenExpired(token);

            if (isValid) {
                logger.debug("JWT token validation successful for user: {}", userDetails.getUsername());
            } else {
                logger.warn("JWT token validation failed for user: {}", userDetails.getUsername());
            }

            return isValid;

        } catch (Exception e) {
            logger.error("Error validating JWT token for user: {}", userDetails.getUsername(), e);
            return false;
        }
    }
}
