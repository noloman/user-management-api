package me.manulorenzo.usermanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.manulorenzo.usermanagement.dto.UserProfile;
import me.manulorenzo.usermanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profile", description = "Endpoints for viewing and updating your user profile. Requires authentication.")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {
    private final UserService userService;

    @Operation(
            summary = "Get current user's profile",
            description = "Returns the current logged-in user's full profile data. Requires a valid JWT access token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile fetched successfully",
                    content = @Content(schema = @Schema(implementation = UserProfile.class))),
            @ApiResponse(responseCode = "401", description = "Access token missing or invalid",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<UserProfile> getProfile(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.getUserProfile(username));
    }

    @Operation(
            summary = "Update current user's profile",
            description = "Updates profile fields for the currently authenticated user. Requires valid JWT access token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserProfile.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data supplied",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Access token missing or invalid",
                    content = @Content)
    })
    @PutMapping
    public ResponseEntity<UserProfile> updateProfile(
            Authentication authentication,
            @RequestBody UserProfile dto
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.updateUserProfile(username, dto));
    }
}

