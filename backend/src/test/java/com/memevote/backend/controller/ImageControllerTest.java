package com.memevote.backend.controller;

import com.memevote.backend.model.ImageData;
import com.memevote.backend.service.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ImageControllerTest {

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private ImageController imageController;

    private MockMvc mockMvc;
    private ImageData imageData;
    private byte[] imageBytes;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController).build();
        
        // Setup image data
        imageBytes = "test image content".getBytes();
        imageData = ImageData.builder()
                .id(1L)
                .name("test-image.jpg")
                .type("image/jpeg")
                .data(imageBytes)
                .fileSize((long) imageBytes.length)
                .build();
    }

    @Test
    void getImage_WhenImageExists_ShouldReturnImage() throws Exception {
        // Arrange
        when(imageStorageService.getImage("test-image.jpg")).thenReturn(Optional.of(imageData));
        
        // Act & Assert
        mockMvc.perform(get("/uploads/test-image.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageBytes));
    }

    @Test
    void getImage_WhenImageDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(imageStorageService.getImage("non-existent.jpg")).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/uploads/non-existent.jpg"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Image not found"));
    }
}
