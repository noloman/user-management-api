package me.manulorenzo.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.manulorenzo.usermanagement.dto.UserProfile;
import me.manulorenzo.usermanagement.exception.GlobalExceptionHandler;
import me.manulorenzo.usermanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProfileControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProfileController profileController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getProfile_ShouldReturnUserProfile_WhenUserExists() throws Exception {
        UserProfile userProfile = new UserProfile();
        userProfile.setUsername("john");
        userProfile.setEmail("john@example.com");
        userProfile.setFullName("John Doe");
        userProfile.setBio("Software Developer");
        userProfile.setImageUrl("http://example.com/image.jpg");

        when(authentication.getName()).thenReturn("john");
        when(userService.getUserProfile("john")).thenReturn(userProfile);

        mockMvc.perform(get("/api/profile")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.bio").value("Software Developer"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/image.jpg"));

        verify(userService).getUserProfile("john");
    }

    @Test
    void getProfile_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        when(authentication.getName()).thenReturn("nonexistent");
        when(userService.getUserProfile("nonexistent"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(get("/api/profile")
                        .principal(authentication))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getProfile_ShouldReturnProfileWithNullFields_WhenProfileIncomplete() throws Exception {
        UserProfile userProfile = new UserProfile();
        userProfile.setUsername("jane");
        userProfile.setEmail("jane@example.com");
        // Other fields are null

        when(authentication.getName()).thenReturn("jane");
        when(userService.getUserProfile("jane")).thenReturn(userProfile);

        mockMvc.perform(get("/api/profile")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jane"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.fullName").isEmpty())
                .andExpect(jsonPath("$.bio").isEmpty())
                .andExpect(jsonPath("$.imageUrl").isEmpty());
    }

    @Test
    void updateProfile_ShouldReturnUpdatedProfile_WhenValidData() throws Exception {
        UserProfile updateRequest = new UserProfile();
        updateRequest.setEmail("updated@example.com");
        updateRequest.setFullName("Updated Name");
        updateRequest.setBio("Updated bio");
        updateRequest.setImageUrl("http://example.com/updated.jpg");

        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setUsername("john");
        updatedProfile.setEmail("updated@example.com");
        updatedProfile.setFullName("Updated Name");
        updatedProfile.setBio("Updated bio");
        updatedProfile.setImageUrl("http://example.com/updated.jpg");

        when(authentication.getName()).thenReturn("john");
        when(userService.updateUserProfile("john", updateRequest)).thenReturn(updatedProfile);

        mockMvc.perform(put("/api/profile")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.bio").value("Updated bio"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/updated.jpg"));

        verify(userService).updateUserProfile("john", updateRequest);
    }

    @Test
    void updateProfile_ShouldReturnConflict_WhenUserServiceThrowsException() throws Exception {
        UserProfile updateRequest = new UserProfile();
        updateRequest.setEmail("existing@example.com");

        when(authentication.getName()).thenReturn("john");
        when(userService.updateUserProfile("john", updateRequest))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(put("/api/profile")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("RESOURCE_CONFLICT"))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void updateProfile_ShouldUpdatePartialFields_WhenOnlySomeFieldsProvided() throws Exception {
        UserProfile updateRequest = new UserProfile();
        updateRequest.setFullName("Only Full Name Updated");
        // Other fields are null

        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setUsername("alice");
        updatedProfile.setEmail("alice@example.com"); // unchanged
        updatedProfile.setFullName("Only Full Name Updated");
        updatedProfile.setBio("Old bio"); // unchanged
        updatedProfile.setImageUrl("http://example.com/old.jpg"); // unchanged

        when(authentication.getName()).thenReturn("alice");
        when(userService.updateUserProfile("alice", updateRequest)).thenReturn(updatedProfile);

        mockMvc.perform(put("/api/profile")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.fullName").value("Only Full Name Updated"))
                .andExpect(jsonPath("$.bio").value("Old bio"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/old.jpg"));
    }

    @Test
    void updateProfile_ShouldHandleEmptyBody_WhenNoFieldsProvided() throws Exception {
        UserProfile emptyRequest = new UserProfile();

        UserProfile currentProfile = new UserProfile();
        currentProfile.setUsername("bob");
        currentProfile.setEmail("bob@example.com");
        currentProfile.setFullName("Bob Smith");
        currentProfile.setBio("Developer");
        currentProfile.setImageUrl("http://example.com/bob.jpg");

        when(authentication.getName()).thenReturn("bob");
        when(userService.updateUserProfile("bob", emptyRequest)).thenReturn(currentProfile);

        mockMvc.perform(put("/api/profile")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.fullName").value("Bob Smith"))
                .andExpect(jsonPath("$.bio").value("Developer"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/bob.jpg"));
    }

    @Test
    void updateProfile_ShouldHandleUsernameChange_WhenUsernameProvided() throws Exception {
        UserProfile updateRequest = new UserProfile();
        updateRequest.setUsername("newusername");
        updateRequest.setEmail("same@example.com");

        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setUsername("newusername");
        updatedProfile.setEmail("same@example.com");
        updatedProfile.setFullName("Same User");
        updatedProfile.setBio("Same bio");
        updatedProfile.setImageUrl("http://example.com/same.jpg");

        when(authentication.getName()).thenReturn("oldusername");
        when(userService.updateUserProfile("oldusername", updateRequest)).thenReturn(updatedProfile);

        mockMvc.perform(put("/api/profile")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"))
                .andExpect(jsonPath("$.email").value("same@example.com"));

        verify(userService).updateUserProfile("oldusername", updateRequest);
    }

    @Test
    void updateProfile_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        UserProfile updateRequest = new UserProfile();
        updateRequest.setFullName("New Name");

        when(authentication.getName()).thenReturn("nonexistent");
        when(userService.updateUserProfile("nonexistent", updateRequest))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(put("/api/profile")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
