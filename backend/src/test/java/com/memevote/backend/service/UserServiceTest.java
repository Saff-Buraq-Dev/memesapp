package com.memevote.backend.service;

import com.memevote.backend.dto.request.ProfileUpdateRequest;
import com.memevote.backend.dto.response.MessageResponse;
import com.memevote.backend.dto.response.UserSummary;
import com.memevote.backend.model.User;
import com.memevote.backend.repository.UserRepository;
import com.memevote.backend.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDetailsImpl userDetails;
    private ProfileUpdateRequest profileUpdateRequest;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setProfilePicture("profile.jpg");

        // Setup UserDetailsImpl
        userDetails = UserDetailsImpl.build(testUser);

        // Setup profile update request
        profileUpdateRequest = new ProfileUpdateRequest();
        profileUpdateRequest.setUsername("newusername");

        // Setup mock file
        mockFile = new MockMultipartFile(
                "file",
                "new-profile.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    void getCurrentUser_ShouldReturnCurrentUser() {
        // Arrange
        try (MockedStatic<SecurityContextHolder> securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // Act
            User result = userService.getCurrentUser();

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("testuser", result.getUsername());
            assertEquals("test@example.com", result.getEmail());

            // Verify
            verify(userRepository).findById(1L);
        }
    }

    @Test
    void getCurrentUser_WhenNotAuthenticated_ShouldThrowException() {
        // Arrange
        try (MockedStatic<SecurityContextHolder> securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn("anonymousUser");

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                userService.getCurrentUser();
            });
            
            assertEquals("User not authenticated", exception.getMessage());
        }
    }

    @Test
    void getUserSummary_ShouldReturnUserSummary() {
        // Act
        UserSummary result = userService.getUserSummary(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("profile.jpg", result.getProfilePicture());
    }

    @Test
    void updateProfile_WithNewUsername_ShouldUpdateUsername() {
        // Arrange
        try (MockedStatic<SecurityContextHolder> securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsername("newusername")).thenReturn(false);

            // Act
            MessageResponse response = userService.updateProfile(profileUpdateRequest);

            // Assert
            assertNotNull(response);
            assertEquals("Profile updated successfully!", response.getMessage());
            assertEquals("newusername", testUser.getUsername());

            // Verify
            verify(userRepository).findById(1L);
            verify(userRepository).existsByUsername("newusername");
            verify(userRepository).save(testUser);
        }
    }

    @Test
    void updateProfile_WithExistingUsername_ShouldReturnError() {
        // Arrange
        try (MockedStatic<SecurityContextHolder> securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsername("newusername")).thenReturn(true);

            // Act
            MessageResponse response = userService.updateProfile(profileUpdateRequest);

            // Assert
            assertNotNull(response);
            assertEquals("Error: Username is already taken!", response.getMessage());
            assertEquals("testuser", testUser.getUsername()); // Username should not change

            // Verify
            verify(userRepository).findById(1L);
            verify(userRepository).existsByUsername("newusername");
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    void updateProfilePicture_ShouldUpdateProfilePicture() throws IOException {
        // Arrange
        try (MockedStatic<SecurityContextHolder> securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(imageStorageService.storeImage(any(MultipartFile.class))).thenReturn("new-profile.jpg");

            // Act
            MessageResponse response = userService.updateProfilePicture(mockFile);

            // Assert
            assertNotNull(response);
            assertEquals("Profile picture updated successfully!", response.getMessage());
            assertEquals("new-profile.jpg", testUser.getProfilePicture());

            // Verify
            verify(userRepository).findById(1L);
            verify(imageStorageService).deleteImage("profile.jpg");
            verify(imageStorageService).storeImage(mockFile);
            verify(userRepository).save(testUser);
        }
    }
}
