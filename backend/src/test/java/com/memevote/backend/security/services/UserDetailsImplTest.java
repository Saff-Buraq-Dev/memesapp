package com.memevote.backend.security.services;

import com.memevote.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class UserDetailsImplTest {

    private User testUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setProfilePicture("profile.jpg");
        
        // Create UserDetailsImpl instance
        userDetails = UserDetailsImpl.build(testUser);
    }

    @Test
    void build_ShouldCreateUserDetailsFromUser() {
        // Assert
        assertEquals(1L, userDetails.getId());
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("test@example.com", userDetails.getEmail());
        assertEquals("password", userDetails.getPassword());
        assertEquals("profile.jpg", userDetails.getProfilePicture());
    }

    @Test
    void getAuthorities_ShouldReturnEmptyList() {
        // Act
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        
        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void isAccountNonExpired_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(userDetails.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(userDetails.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void equals_WithSameObject_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(userDetails.equals(userDetails));
    }

    @Test
    void equals_WithNull_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(userDetails.equals(null));
    }

    @Test
    void equals_WithDifferentClass_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(userDetails.equals("string"));
    }

    @Test
    void equals_WithSameId_ShouldReturnTrue() {
        // Arrange
        UserDetailsImpl otherUserDetails = new UserDetailsImpl(
                1L, "otheruser", "other@example.com", "otherpassword", "other.jpg");
        
        // Act & Assert
        assertTrue(userDetails.equals(otherUserDetails));
    }

    @Test
    void equals_WithDifferentId_ShouldReturnFalse() {
        // Arrange
        UserDetailsImpl otherUserDetails = new UserDetailsImpl(
                2L, "testuser", "test@example.com", "password", "profile.jpg");
        
        // Act & Assert
        assertFalse(userDetails.equals(otherUserDetails));
    }
}
