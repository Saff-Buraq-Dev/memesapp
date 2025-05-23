package com.memevote.backend.controller;

import com.memevote.backend.model.ImageData;
import com.memevote.backend.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/uploads")
public class ImageController {

    @Autowired
    private ImageStorageService imageStorageService;

    @GetMapping("/{fileName}")
    public ResponseEntity<?> getImage(@PathVariable String fileName) {
        Optional<ImageData> imageDataOptional = imageStorageService.getImage(fileName);
        
        if (imageDataOptional.isPresent()) {
            ImageData imageData = imageDataOptional.get();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(imageData.getType()))
                    .body(imageData.getData());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
    }
}
