package me.manulorenzo.usermanagement.controller;

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

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/addRole")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addRole(@RequestParam String username, @RequestParam String roleName) {
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
}
