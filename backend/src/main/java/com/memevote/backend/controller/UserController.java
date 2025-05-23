package com.memevote.backend.controller;

import com.memevote.backend.dto.request.ProfileUpdateRequest;
import com.memevote.backend.dto.response.MessageResponse;
import com.memevote.backend.dto.response.UserSummary;
import com.memevote.backend.model.User;
import com.memevote.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserSummary> getCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(userService.getUserSummary(currentUser));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        MessageResponse response = userService.updateProfile(profileUpdateRequest);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/profile-picture")
    public ResponseEntity<?> updateProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            MessageResponse response = userService.updateProfilePicture(file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}
