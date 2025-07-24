package me.manulorenzo.usermanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.manulorenzo.usermanagement.security.JwtUtil;
import me.manulorenzo.usermanagement.dto.LoginRequest;
import me.manulorenzo.usermanagement.dto.LoginResponse;
import me.manulorenzo.usermanagement.dto.RegisterRequest;
import me.manulorenzo.usermanagement.dto.RefreshTokenRequest;
import me.manulorenzo.usermanagement.dto.RefreshTokenResponse;
import me.manulorenzo.usermanagement.entity.RefreshToken;
import me.manulorenzo.usermanagement.service.RefreshTokenService;
import me.manulorenzo.usermanagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "User registration and login endpoints")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;

    public AuthController(UserService userService, AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil, RefreshTokenService refreshTokenService,
                          UserDetailsService userDetailsService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account. The first user gets ADMIN role, subsequent users get USER role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Registration failed - username already exists or invalid data",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        logger.info("Registration request received for username: {}", request.getUsername());

        try {
            userService.register(request);
            logger.info("User {} registered successfully", request.getUsername());
            return ResponseEntity.ok("User registered");
        } catch (RuntimeException e) {
            logger.error("Registration failed for username {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during registration for username {}", request.getUsername(), e);
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Login user",
            description = "Authenticates user credentials and returns JWT access token and refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Login failed - invalid credentials",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("Login attempt for username: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails user = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtUtil.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());

            logger.info("Login successful for username: {} with authorities: {}",
                    request.getUsername(), user.getAuthorities());

            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken.getToken()));
        } catch (Exception e) {
            logger.error("Login failed for username {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Refresh access token",
            description = "Uses refresh token to generate a new access token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        logger.info("Token refresh request received");

        try {
            String requestRefreshToken = request.getRefreshToken();

            RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));

            refreshToken = refreshTokenService.verifyExpiration(refreshToken);

            UserDetails userDetails = userDetailsService.loadUserByUsername(refreshToken.getUser().getUsername());
            String newAccessToken = jwtUtil.generateToken(userDetails);

            logger.info("Token refreshed successfully for user: {}", refreshToken.getUser().getUsername());

            return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken));

        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Token refresh failed: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Logout user",
            description = "Invalidates the user's refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Logout failed",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        logger.info("Logout request received");

        try {
            refreshTokenService.deleteByToken(request.getRefreshToken());
            logger.info("Logout successful");
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Logout failed: " + e.getMessage());
        }
    }
}
