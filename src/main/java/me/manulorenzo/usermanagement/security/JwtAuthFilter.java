package me.manulorenzo.usermanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.manulorenzo.usermanagement.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        logger.debug("Processing request to: {} with Authorization header: {}",
                requestURI, authHeader != null ? "present" : "absent");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.debug("JWT token extracted from Authorization header");

            try {
                String username = jwtUtil.extractUsername(token);
                logger.debug("Username extracted from token: {}", username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    logger.debug("No existing authentication found, validating token for user: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        logger.info("Authentication successful for user: {} accessing: {}", username, requestURI);
                    } else {
                        logger.warn("JWT token validation failed for user: {} accessing: {}", username, requestURI);
                    }
                } else if (username != null) {
                    logger.debug("User {} already authenticated, skipping token validation", username);
                }

            } catch (Exception e) {
                logger.error("Error processing JWT token for request to {}: {}", requestURI, e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.debug("No Bearer token found in request to: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }
}

