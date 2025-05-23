package com.memevote.backend.service;

import com.memevote.backend.model.ImageData;
import com.memevote.backend.repository.ImageDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Autowired
    private ImageDataRepository imageDataRepository;

    public String storeImage(MultipartFile file) throws IOException {
        // Generate a unique file name
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        // Create and save the image data
        ImageData imageData = ImageData.builder()
                .name(fileName)
                .type(file.getContentType())
                .data(file.getBytes())
                .fileSize(file.getSize())
                .build();
        
        imageDataRepository.save(imageData);
        
        return fileName;
    }
    
    public Optional<ImageData> getImage(String fileName) {
        return imageDataRepository.findByName(fileName);
    }
    
    public void deleteImage(String fileName) {
        Optional<ImageData> imageData = imageDataRepository.findByName(fileName);
        imageData.ifPresent(imageDataRepository::delete);
    }
}
