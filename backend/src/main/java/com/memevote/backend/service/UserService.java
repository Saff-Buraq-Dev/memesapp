package com.memevote.backend.service;

import com.memevote.backend.dto.request.ProfileUpdateRequest;
import com.memevote.backend.dto.response.MessageResponse;
import com.memevote.backend.dto.response.UserSummary;
import com.memevote.backend.model.User;
import com.memevote.backend.repository.UserRepository;
import com.memevote.backend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new RuntimeException("User not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserSummary getUserSummary(User user) {
        return new UserSummary(user.getId(), user.getUsername(), user.getEmail(), user.getProfilePicture());
    }

    public MessageResponse updateProfile(ProfileUpdateRequest profileUpdateRequest) {
        User currentUser = getCurrentUser();

        if (profileUpdateRequest.getUsername() != null &&
            !profileUpdateRequest.getUsername().equals(currentUser.getUsername())) {

            if (userRepository.existsByUsername(profileUpdateRequest.getUsername())) {
                return new MessageResponse("Error: Username is already taken!");
            }

            currentUser.setUsername(profileUpdateRequest.getUsername());
            userRepository.save(currentUser);
        }

        return new MessageResponse("Profile updated successfully!");
    }

    public MessageResponse updateProfilePicture(MultipartFile file) throws IOException {
        User currentUser = getCurrentUser();

        // Delete old profile picture if exists
        if (currentUser.getProfilePicture() != null) {
            imageStorageService.deleteImage(currentUser.getProfilePicture());
        }

        // Store new profile picture
        String fileName = imageStorageService.storeImage(file);
        currentUser.setProfilePicture(fileName);
        userRepository.save(currentUser);

        return new MessageResponse("Profile picture updated successfully!");
    }
}
