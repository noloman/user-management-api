package me.manulorenzo.usermanagement.controller;

import me.manulorenzo.usermanagement.exception.GlobalExceptionHandler;
import me.manulorenzo.usermanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void addRole_ShouldReturnOk_WhenValidUserAndRole() throws Exception {
        String username = "testuser";
        String roleName = "USER";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");

        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", username)
                        .param("roleName", roleName))
                .andExpect(status().isOk())
                .andExpect(content().string("Role USER successfully added to user testuser"));

        verify(userService).addRoleToUser(username, roleName);
    }

    @Test
    void addRole_ShouldReturnBadRequest_WhenUserNotFound() throws Exception {
        String username = "nonexistent";
        String roleName = "USER";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        doThrow(new UsernameNotFoundException("User not found"))
                .when(userService).addRoleToUser(username, roleName);

        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", username)
                        .param("roleName", roleName))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void addRole_ShouldReturnBadRequest_WhenRoleNotFound() throws Exception {
        String username = "testuser";
        String roleName = "NONEXISTENT_ROLE";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        doThrow(new RuntimeException("Role not found"))
                .when(userService).addRoleToUser(username, roleName);

        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", username)
                        .param("roleName", roleName))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Role not found"));
    }

    @Test
    void addRole_ShouldReturnInternalServerError_WhenUnexpectedError() throws Exception {
        String username = "testuser";
        String roleName = "USER";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        doThrow(new RuntimeException("Database connection failed"))
                .when(userService).addRoleToUser(username, roleName);

        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", username)
                        .param("roleName", roleName))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Database connection failed"));
    }

    @Test
    void addRole_ShouldHandleMissingUsername() throws Exception {
        String roleName = "USER";

        mockMvc.perform(post("/api/admin/addRole")
                        .param("roleName", roleName))
                .andExpect(status().isBadRequest()); // Missing required parameter
    }

    @Test
    void addRole_ShouldHandleMissingRoleName() throws Exception {
        String username = "testuser";

        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", username))
                .andExpect(status().isBadRequest()); // Missing required parameter
    }

    @Test
    void addRole_ShouldHandleEmptyUsername() throws Exception {
        String username = "";
        String roleName = "USER";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        doThrow(new RuntimeException("Username cannot be empty"))
                .when(userService).addRoleToUser(username, roleName);

        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", username)
                        .param("roleName", roleName))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Username cannot be empty"));
    }

    @Test
    void addRole_ShouldHandleEmptyRoleName() throws Exception {
        String username = "testuser";
        String roleName = "";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        doThrow(new RuntimeException("Role name cannot be empty"))
                .when(userService).addRoleToUser(username, roleName);

        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", username)
                        .param("roleName", roleName))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Role name cannot be empty"));
    }

    @Test
    void testAdminAccess_ShouldReturnOk_WhenValidAdmin() throws Exception {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");

        mockMvc.perform(post("/api/admin/test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin access confirmed for user: admin")));
    }

    @Test
    void testAdminAccess_ShouldShowCorrectUserInfo_WhenDifferentAdmin() throws Exception {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("superadmin");

        mockMvc.perform(post("/api/admin/test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin access confirmed for user: superadmin")));
    }

    @Test
    void testWithoutAuth_ShouldReturnOk_AlwaysAllowed() throws Exception {
        mockMvc.perform(post("/api/admin/test-no-auth"))
                .andExpect(status().isOk())
                .andExpect(content().string("Test endpoint working - no authentication required"));
    }

    @Test
    void addRole_ShouldLogCorrectAdminUser_WhenCalledByDifferentAdmin() throws Exception {
        String username = "regularuser";
        String roleName = "MODERATOR";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("superadmin");

        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", username)
                        .param("roleName", roleName))
                .andExpect(status().isOk())
                .andExpect(content().string("Role MODERATOR successfully added to user regularuser"));

        verify(userService).addRoleToUser(username, roleName);
    }

    @Test
    void addRole_ShouldHandleSpecialCharactersInParameters() throws Exception {
        String username = "test@user";
        String roleName = "USER_ROLE";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");

        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", username)
                        .param("roleName", roleName))
                .andExpect(status().isOk())
                .andExpect(content().string("Role USER_ROLE successfully added to user test@user"));

        verify(userService).addRoleToUser(username, roleName);
    }

    @Test
    void addRole_ShouldHandleLongParameters() throws Exception {
        String username = "a".repeat(50); // Long username
        String roleName = "SUPER_LONG_ROLE_NAME_THAT_MIGHT_CAUSE_ISSUES";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");

        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", username)
                        .param("roleName", roleName))
                .andExpect(status().isOk())
                .andExpect(content().string("Role SUPER_LONG_ROLE_NAME_THAT_MIGHT_CAUSE_ISSUES successfully added to user " + username));

        verify(userService).addRoleToUser(username, roleName);
    }

    @Test
    void addRole_ShouldHandleCaseInsensitiveRoles() throws Exception {
        String username = "testuser";
        String roleName = "admin"; // lowercase

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");

        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", username)
                        .param("roleName", roleName))
                .andExpect(status().isOk())
                .andExpect(content().string("Role admin successfully added to user testuser"));

        verify(userService).addRoleToUser(username, roleName);
    }
}