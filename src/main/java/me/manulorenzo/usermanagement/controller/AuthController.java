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
import me.manulorenzo.usermanagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
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
            description = "Authenticates user credentials and returns JWT token for API access"
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
            String jwt = jwtUtil.generateToken(user);

            logger.info("Login successful for username: {} with authorities: {}",
                    request.getUsername(), user.getAuthorities());

            return ResponseEntity.ok(new LoginResponse(jwt));
        } catch (Exception e) {
            logger.error("Login failed for username {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }
}
