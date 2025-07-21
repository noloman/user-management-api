package me.manulorenzo.usermanagement.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public JwtUtil() {
        logger.info("JwtUtil initialized with new secret key");
    }

    public String generateToken(UserDetails userDetails) {
        logger.debug("Generating JWT token for user: {}", userDetails.getUsername());

        try {
            String token = Jwts.builder()
                    .setSubject(userDetails.getUsername())
                    .claim("roles", userDetails.getAuthorities())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
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

    public boolean validateToken(String token, UserDetails userDetails) {
        logger.debug("Validating JWT token for user: {}", userDetails.getUsername());

        try {
            String extractedUsername = extractUsername(token);
            boolean isValid = extractedUsername.equals(userDetails.getUsername());

            if (isValid) {
                logger.debug("JWT token validation successful for user: {}", userDetails.getUsername());
            } else {
                logger.warn("JWT token validation failed - username mismatch. Expected: {}, Got: {}",
                        userDetails.getUsername(), extractedUsername);
            }

            return isValid;

        } catch (Exception e) {
            logger.error("Error validating JWT token for user: {}", userDetails.getUsername(), e);
            return false;
        }
    }
}
