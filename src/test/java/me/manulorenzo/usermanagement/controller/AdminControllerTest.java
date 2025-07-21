package me.manulorenzo.usermanagement.controller;

import me.manulorenzo.usermanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        // Mock security context
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void addRole_ShouldReturnOk_WhenValidRequest() throws Exception {
        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", "john")
                        .param("roleName", "MODERATOR"))
                .andExpect(status().isOk())
                .andExpect(content().string("Role MODERATOR successfully added to user john"));

        verify(userService).addRoleToUser("john", "MODERATOR");
    }

    @Test
    void addRole_ShouldReturnBadRequest_WhenUserServiceThrows() throws Exception {
        doThrow(new RuntimeException("User not found")).when(userService).addRoleToUser("nonexistent", "ADMIN");

        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", "nonexistent")
                        .param("roleName", "ADMIN"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to add role: User not found"));

        verify(userService).addRoleToUser("nonexistent", "ADMIN");
    }
}