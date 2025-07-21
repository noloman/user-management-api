package me.manulorenzo.usermanagement.controller;

import me.manulorenzo.usermanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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
    }

    @Test
    void addRole_ShouldReturnOk_WhenValidRequest() throws Exception {
        mockMvc.perform(post("/api/admin/addRole")
                        .param("username", "john")
                        .param("roleName", "MODERATOR"))
                .andExpect(status().isOk())
                .andExpect(content().string("Role added to user"));

        verify(userService).addRoleToUser("john", "MODERATOR");
    }

    @Test
    void addRole_ShouldThrowException_WhenUserServiceThrows() {
        doThrow(new RuntimeException("User not found")).when(userService).addRoleToUser("nonexistent", "ADMIN");

        assertThrows(Exception.class, () -> mockMvc.perform(post("/api/admin/addRole")
                .param("username", "nonexistent")
                .param("roleName", "ADMIN")));
    }
}