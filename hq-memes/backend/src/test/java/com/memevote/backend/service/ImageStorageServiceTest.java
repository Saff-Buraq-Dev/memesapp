package com.memevote.backend.service;

import com.memevote.backend.model.ImageData;
import com.memevote.backend.repository.ImageDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageStorageServiceTest {

    @Mock
    private ImageDataRepository imageDataRepository;

    @InjectMocks
    private ImageStorageService imageStorageService;

    private MultipartFile mockFile;
    private ImageData imageData;

    @BeforeEach
    void setUp() {
        // Setup mock file
        mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Setup image data
        imageData = ImageData.builder()
                .id(1L)
                .name("test-uuid.jpg")
                .type("image/jpeg")
                .data("test image content".getBytes())
                .fileSize(mockFile.getSize())
                .build();
    }

    @Test
    void storeImage_ShouldReturnFileName() throws IOException {
        // Arrange
        when(imageDataRepository.save(any(ImageData.class))).thenReturn(imageData);

        // Act
        String fileName = imageStorageService.storeImage(mockFile);

        // Assert
        assertNotNull(fileName);
        assertTrue(fileName.endsWith(".jpg"));

        // Verify
        ArgumentCaptor<ImageData> imageDataCaptor = ArgumentCaptor.forClass(ImageData.class);
        verify(imageDataRepository).save(imageDataCaptor.capture());
        
        ImageData capturedImageData = imageDataCaptor.getValue();
        assertEquals("image/jpeg", capturedImageData.getType());
        assertEquals(mockFile.getSize(), capturedImageData.getFileSize());
        assertArrayEquals(mockFile.getBytes(), capturedImageData.getData());
    }

    @Test
    void getImage_WhenImageExists_ShouldReturnImageData() {
        // Arrange
        when(imageDataRepository.findByName("test-uuid.jpg")).thenReturn(Optional.of(imageData));

        // Act
        Optional<ImageData> result = imageStorageService.getImage("test-uuid.jpg");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(imageData, result.get());

        // Verify
        verify(imageDataRepository).findByName("test-uuid.jpg");
    }

    @Test
    void getImage_WhenImageDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        when(imageDataRepository.findByName("non-existent.jpg")).thenReturn(Optional.empty());

        // Act
        Optional<ImageData> result = imageStorageService.getImage("non-existent.jpg");

        // Assert
        assertFalse(result.isPresent());

        // Verify
        verify(imageDataRepository).findByName("non-existent.jpg");
    }

    @Test
    void deleteImage_WhenImageExists_ShouldDeleteImage() {
        // Arrange
        when(imageDataRepository.findByName("test-uuid.jpg")).thenReturn(Optional.of(imageData));

        // Act
        imageStorageService.deleteImage("test-uuid.jpg");

        // Verify
        verify(imageDataRepository).findByName("test-uuid.jpg");
        verify(imageDataRepository).delete(imageData);
    }

    @Test
    void deleteImage_WhenImageDoesNotExist_ShouldDoNothing() {
        // Arrange
        when(imageDataRepository.findByName("non-existent.jpg")).thenReturn(Optional.empty());

        // Act
        imageStorageService.deleteImage("non-existent.jpg");

        // Verify
        verify(imageDataRepository).findByName("non-existent.jpg");
        verify(imageDataRepository, never()).delete(any(ImageData.class));
    }
}
