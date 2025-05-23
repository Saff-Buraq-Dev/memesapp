package com.memevote.backend.service;

import com.memevote.backend.dto.request.MemeRequest;
import com.memevote.backend.dto.response.MemeResponse;
import com.memevote.backend.dto.response.UserSummary;
import com.memevote.backend.dto.response.VoterDto;
import com.memevote.backend.dto.websocket.WebSocketEvent;
import com.memevote.backend.model.Category;
import com.memevote.backend.model.Meme;
import com.memevote.backend.model.User;
import com.memevote.backend.repository.MemeRepository;
import com.memevote.backend.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MemeService {
    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public MemeResponse createMeme(MemeRequest memeRequest, MultipartFile file) throws IOException {
        User currentUser = userService.getCurrentUser();

        // Store the file in the database
        String fileName = imageStorageService.storeImage(file);

        // Create new meme
        Meme meme = new Meme();
        meme.setTitle(memeRequest.getTitle());
        meme.setUrl(fileName);
        meme.setUser(currentUser);

        // Add categories if any (they're optional)
        if (memeRequest.getCategories() != null && !memeRequest.getCategories().isEmpty()) {
            Set<Category> categories = categoryService.getCategoriesByNames(memeRequest.getCategories());
            meme.setCategories(categories);
        }

        meme = memeRepository.save(meme);

        // Notify subscribers about new meme
        MemeResponse memeResponse = mapToMemeResponse(meme, currentUser);
        WebSocketEvent<MemeResponse> event = new WebSocketEvent<>();
        event.setType("NEW_MEME");
        event.setPayload(memeResponse);
        messagingTemplate.convertAndSend("/topic/memes", event);

        return memeResponse;
    }

    public List<MemeResponse> createMemes(MultipartFile[] files, Set<String> categories) throws IOException {
        // Get current user - authentication is required
        User currentUser = userService.getCurrentUser();

        List<MemeResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            // Store the file in the database
            String fileName = imageStorageService.storeImage(file);

            // Extract original filename without extension for title
            String originalFilename = file.getOriginalFilename();
            String title = originalFilename;
            if (originalFilename != null && originalFilename.contains(".")) {
                title = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            }

            // Create new meme
            Meme meme = new Meme();
            meme.setTitle(title);
            meme.setUrl(fileName);
            meme.setUser(currentUser);

            // Add categories if any (they're optional)
            if (categories != null && !categories.isEmpty()) {
                Set<Category> categoryEntities = categoryService.getCategoriesByNames(categories);
                meme.setCategories(categoryEntities);
            }

            meme = memeRepository.save(meme);

            // Create response
            MemeResponse memeResponse = mapToMemeResponse(meme, currentUser);
            responses.add(memeResponse);

            // Notify subscribers about new meme
            WebSocketEvent<MemeResponse> event = new WebSocketEvent<>();
            event.setType("NEW_MEME");
            event.setPayload(memeResponse);
            messagingTemplate.convertAndSend("/topic/memes", event);
        }

        return responses;
    }

    public Page<MemeResponse> getMemes(Set<String> categoryNames, String username, String title, Pageable pageable) {
        // Get current user for use in lambda
        User currentUserTemp = null;
        try {
            currentUserTemp = userService.getCurrentUser();
        } catch (Exception e) {
            // User is not authenticated - currentUser remains null
        }
        final User currentUser = currentUserTemp;

        User filterUser = null;
        if (username != null && !username.isEmpty()) {
            // TODO: Implement user lookup by username
        }

        Set<Category> categories = null;
        if (categoryNames != null && !categoryNames.isEmpty()) {
            categories = categoryService.getCategoriesByNames(categoryNames);
        }

        Page<Meme> memePage;

        if (categories != null && filterUser != null && title != null && !title.isEmpty()) {
            memePage = memeRepository.findByCategoriesInAndUserAndTitleContainingIgnoreCase(categories, filterUser, title, pageable);
        } else if (categories != null && filterUser != null) {
            memePage = memeRepository.findByCategoriesInAndUser(categories, filterUser, pageable);
        } else if (categories != null && title != null && !title.isEmpty()) {
            memePage = memeRepository.findByCategoriesInAndTitleContainingIgnoreCase(categories, title, pageable);
        } else if (filterUser != null && title != null && !title.isEmpty()) {
            memePage = memeRepository.findByUserAndTitleContainingIgnoreCase(filterUser, title, pageable);
        } else if (categories != null) {
            memePage = memeRepository.findByCategoriesIn(categories, pageable);
        } else if (filterUser != null) {
            memePage = memeRepository.findByUser(filterUser, pageable);
        } else if (title != null && !title.isEmpty()) {
            memePage = memeRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else {
            memePage = memeRepository.findAll(pageable);
        }

        return memePage.map(meme -> mapToMemeResponse(meme, currentUser));
    }

    public MemeResponse getMemeById(Long id) {
        // Get current user for use in lambda
        User currentUserTemp = null;
        try {
            currentUserTemp = userService.getCurrentUser();
        } catch (Exception e) {
            // User is not authenticated - currentUser remains null
        }
        final User currentUser = currentUserTemp;

        Meme meme = memeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meme not found"));

        return mapToMemeResponse(meme, currentUser);
    }

    private MemeResponse mapToMemeResponse(Meme meme, User currentUser) {
        MemeResponse response = new MemeResponse();
        response.setId(meme.getId());
        response.setTitle(meme.getTitle());
        response.setUrl(meme.getUrl());
        response.setCreatedAt(meme.getCreatedAt());

        // Handle null user case
        if (meme.getUser() != null) {
            response.setUser(userService.getUserSummary(meme.getUser()));
        } else {
            // Create an anonymous user summary
            UserSummary anonymousUser = new UserSummary();
            anonymousUser.setUsername("Anonymous");
            anonymousUser.setProfilePicture(null);
            response.setUser(anonymousUser);
        }

        // Handle categories
        if (meme.getCategories() != null) {
            response.setCategories(categoryService.mapToCategoryDtoSet(meme.getCategories()));
        }

        // Count votes
        Long voteCount = 0L;
        if (meme.getId() != null) {
            voteCount = voteRepository.countByMemeId(meme.getId());
        }
        response.setVoteCount(voteCount);

        // Check if current user has voted
        if (currentUser != null && meme.getId() != null) {
            Boolean userVoted = voteRepository.existsByUserAndMeme(currentUser, meme);
            response.setUserVoted(userVoted);
        } else {
            response.setUserVoted(false);
        }

        // Get voters
        if (meme.getId() != null) {
            List<User> voters = voteRepository.findVotersByMemeId(meme.getId());
            List<VoterDto> voterDtos = voters.stream()
                .map(voter -> {
                    VoterDto voterDto = new VoterDto();
                    voterDto.setId(voter.getId());
                    voterDto.setUsername(voter.getUsername());
                    voterDto.setProfilePicture(voter.getProfilePicture() != null ?
                        voter.getProfilePicture() : "default-avatar.png");
                    return voterDto;
                })
                .collect(java.util.stream.Collectors.toList());
            response.setVoters(voterDtos);
        }

        return response;
    }
}
