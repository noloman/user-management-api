package me.manulorenzo.usermanagement.config;

import me.manulorenzo.usermanagement.security.JwtAuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtFilter) throws Exception {
        logger.info("Configuring Security Filter Chain");

        SecurityFilterChain chain = http
                .csrf(csrf -> {
                    logger.debug("Disabling CSRF protection");
                    csrf.disable();
                })
                .sessionManagement(session -> {
                    logger.debug("Setting session creation policy to STATELESS");
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .authorizeHttpRequests(auth -> {
                    logger.debug("Configuring authorization rules");
                    auth.requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                            .requestMatchers("/h2-console/**").permitAll()
                            .anyRequest().authenticated();
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

        logger.info("Security Filter Chain configured successfully");
        return chain;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Creating BCrypt Password Encoder bean");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        logger.info("Creating Authentication Manager bean");
        return authConfig.getAuthenticationManager();
    }
}
