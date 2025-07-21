package me.manulorenzo.usermanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.manulorenzo.usermanagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "Administrative operations (requires ADMIN role)")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Add role to user",
            description = "Adds a specific role to an existing user. Only accessible by users with ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role added successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - user or role not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/addRole")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addRole(
            @Parameter(description = "Username of the user to add role to", required = true)
            @RequestParam String username,
            @Parameter(description = "Name of the role to add (e.g., USER, ADMIN, MODERATOR)", required = true)
            @RequestParam String roleName) {
        try {
            logger.info("Admin user {} is trying to add role {} to user {}",
                    SecurityContextHolder.getContext().getAuthentication().getName(),
                    roleName, username);

            userService.addRoleToUser(username, roleName);

            logger.info("Successfully added role {} to user {}", roleName, username);
            return ResponseEntity.ok("Role " + roleName + " successfully added to user " + username);

        } catch (RuntimeException e) {
            logger.error("Failed to add role {} to user {}: {}", roleName, username, e.getMessage());
            return ResponseEntity.badRequest().body("Failed to add role: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while adding role {} to user {}", roleName, username, e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }

    @Operation(
            summary = "Test admin access",
            description = "Test endpoint to verify admin authentication and authorization"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin access confirmed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    })
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testAdminAccess() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        logger.info("Admin test endpoint accessed by user: {} with authorities: {}", currentUser, authorities);

        return ResponseEntity.ok("Admin access confirmed for user: " + currentUser + " with authorities: " + authorities);
    }

    @Operation(
            summary = "Test endpoint without authentication",
            description = "Test endpoint for debugging purposes - no authentication required"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test successful")
    })
    @PostMapping("/test-no-auth")
    public ResponseEntity<String> testWithoutAuth() {
        logger.info("Test endpoint accessed without authentication");
        return ResponseEntity.ok("Test endpoint working - no authentication required");
    }
}
