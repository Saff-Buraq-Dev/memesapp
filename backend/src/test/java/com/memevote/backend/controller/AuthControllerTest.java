package com.memevote.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memevote.backend.dto.request.LoginRequest;
import com.memevote.backend.dto.request.SignupRequest;
import com.memevote.backend.dto.response.JwtResponse;
import com.memevote.backend.dto.response.MessageResponse;
import com.memevote.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private JwtResponse jwtResponse;
    private MessageResponse successResponse;
    private MessageResponse errorResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        
        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");
        
        // Setup signup request
        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password");
        
        // Setup JWT response
        jwtResponse = new JwtResponse(
                "testToken",
                1L,
                "testuser",
                "test@example.com",
                "profile.jpg"
        );
        
        // Setup message responses
        successResponse = new MessageResponse("User registered successfully!");
        errorResponse = new MessageResponse("Error: Username is already taken!");
    }

    @Test
    void authenticateUser_ShouldReturnJwtResponse() throws Exception {
        // Arrange
        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(jwtResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("testToken"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.profilePicture").value("profile.jpg"));
    }

    @Test
    void registerUser_WithSuccess_ShouldReturnOkResponse() throws Exception {
        // Arrange
        when(authService.registerUser(any(SignupRequest.class))).thenReturn(successResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    void registerUser_WithError_ShouldReturnBadRequestResponse() throws Exception {
        // Arrange
        when(authService.registerUser(any(SignupRequest.class))).thenReturn(errorResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }
}
