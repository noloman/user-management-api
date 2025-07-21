package me.manulorenzo.usermanagement.controller;

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

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserProfile> getProfile(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.getUserProfile(username));
    }

    @PutMapping
    public ResponseEntity<UserProfile> updateProfile(
            Authentication authentication,
            @RequestBody UserProfile dto
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.updateUserProfile(username, dto));
    }
}

