package com.memevote.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memevote.backend.dto.request.ProfileUpdateRequest;
import com.memevote.backend.dto.response.MessageResponse;
import com.memevote.backend.dto.response.UserSummary;
import com.memevote.backend.model.User;
import com.memevote.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User user;
    private UserSummary userSummary;
    private ProfileUpdateRequest profileUpdateRequest;
    private MessageResponse successResponse;
    private MessageResponse errorResponse;
    private MockMultipartFile profilePicture;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        // Setup user
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setProfilePicture("profile.jpg");

        // Setup user summary
        userSummary = new UserSummary(1L, "testuser", "test@example.com", "profile.jpg");

        // Setup profile update request
        profileUpdateRequest = new ProfileUpdateRequest();
        profileUpdateRequest.setUsername("newusername");

        // Setup message responses
        successResponse = new MessageResponse("Profile updated successfully");
        errorResponse = new MessageResponse("Error: Username is already taken");

        // Setup profile picture
        profilePicture = new MockMultipartFile(
                "file",
                "new-profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );
    }

    @Test
    void getCurrentUser_ShouldReturnUserSummary() throws Exception {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(user);
        when(userService.getUserSummary(user)).thenReturn(userSummary);

        // Act & Assert
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.profilePicture").value("profile.jpg"));
    }

    @Test
    void updateProfile_WithSuccess_ShouldReturnOkResponse() throws Exception {
        // Arrange
        when(userService.updateProfile(any(ProfileUpdateRequest.class))).thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
    }

    @Test
    void updateProfile_WithError_ShouldReturnBadRequestResponse() throws Exception {
        // Arrange
        when(userService.updateProfile(any(ProfileUpdateRequest.class))).thenReturn(errorResponse);

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken"));
    }

    @Test
    void updateProfilePicture_WithSuccess_ShouldReturnOkResponse() throws Exception {
        // Arrange
        when(userService.updateProfilePicture(any())).thenReturn(new MessageResponse("Profile picture updated successfully"));

        // Act & Assert
        mockMvc.perform(multipart("/api/users/me/profile-picture")
                .file(profilePicture))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile picture updated successfully"));
    }
}
