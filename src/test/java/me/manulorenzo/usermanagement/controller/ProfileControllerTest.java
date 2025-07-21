package me.manulorenzo.usermanagement.controller;

import me.manulorenzo.usermanagement.dto.UserProfile;
import me.manulorenzo.usermanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.containsString;

public class ProfileControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProfileController profileController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(profileController).build();
    }

    @Test
    void testGetProfile() throws Exception {
        when(authentication.getName()).thenReturn("john");
        UserProfile mockProfile = new UserProfile("john", "john@example.com", "John Doe", "about", "img");
        when(userService.getUserProfile("john")).thenReturn(mockProfile);

        mockMvc.perform(get("/api/profile")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("john@example.com")));
        verify(userService).getUserProfile("john");
    }

    @Test
    void testUpdateProfile() throws Exception {
        when(authentication.getName()).thenReturn("jane");
        UserProfile updated = new UserProfile("jane", "jane@example.com", "Jane", "bio here", "url");
        when(userService.updateUserProfile(eq("jane"), any(UserProfile.class))).thenReturn(updated);

        String jsonBody = "{\"username\":\"jane\",\"email\":\"jane@example.com\",\"fullName\":\"Jane\",\"bio\":\"bio here\",\"imageUrl\":\"url\"}";
        mockMvc.perform(put("/api/profile")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jane@example.com")))
                .andExpect(content().string(containsString("bio here")));
        verify(userService).updateUserProfile(eq("jane"), any(UserProfile.class));
    }
}
