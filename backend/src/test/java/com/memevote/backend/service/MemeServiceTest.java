package com.memevote.backend.service;

import com.memevote.backend.dto.request.MemeRequest;
import com.memevote.backend.dto.response.CategoryDto;
import com.memevote.backend.dto.response.MemeResponse;
import com.memevote.backend.dto.response.UserSummary;
import com.memevote.backend.dto.response.VoterDto;
import com.memevote.backend.model.Category;
import com.memevote.backend.model.Meme;
import com.memevote.backend.model.User;
import com.memevote.backend.repository.MemeRepository;
import com.memevote.backend.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.core.MessagePostProcessor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemeServiceTest {

    @Mock
    private MemeRepository memeRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MemeService memeService;

    private User testUser;
    private Meme testMeme;
    private Category testCategory;
    private Set<Category> categories;
    private MemeRequest memeRequest;
    private MultipartFile mockFile;
    private UserSummary userSummary;
    private CategoryDto categoryDto;
    private Set<CategoryDto> categoryDtos;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setProfilePicture("profile.jpg");

        // Setup test category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Funny");
        categories = new HashSet<>();
        categories.add(testCategory);

        // Setup test meme
        testMeme = new Meme();
        testMeme.setId(1L);
        testMeme.setTitle("Test Meme");
        testMeme.setUrl("test-meme.jpg");
        testMeme.setUser(testUser);
        testMeme.setCategories(categories);
        testMeme.setCreatedAt(LocalDateTime.now());

        // Setup meme request
        memeRequest = new MemeRequest();
        memeRequest.setTitle("Test Meme");
        Set<String> categoryNames = new HashSet<>();
        categoryNames.add("Funny");
        memeRequest.setCategories(categoryNames);

        // Setup mock file
        mockFile = new MockMultipartFile(
                "file",
                "test-meme.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Setup UserSummary
        userSummary = new UserSummary();
        userSummary.setId(1L);
        userSummary.setUsername("testuser");
        userSummary.setProfilePicture("profile.jpg");

        // Setup CategoryDto
        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Funny");
        categoryDtos = new HashSet<>();
        categoryDtos.add(categoryDto);
    }

    @Test
    void createMeme_ShouldReturnMemeResponse() throws IOException {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(imageStorageService.storeImage(any(MultipartFile.class))).thenReturn("test-meme.jpg");
        when(categoryService.getCategoriesByNames(anySet())).thenReturn(categories);
        when(memeRepository.save(any(Meme.class))).thenReturn(testMeme);
        when(userService.getUserSummary(any(User.class))).thenReturn(userSummary);
        when(categoryService.mapToCategoryDtoSet(anySet())).thenReturn(categoryDtos);
        when(voteRepository.countByMemeId(anyLong())).thenReturn(0L);
        when(voteRepository.existsByUserAndMeme(any(User.class), any(Meme.class))).thenReturn(false);
        when(voteRepository.findVotersByMemeId(anyLong())).thenReturn(Collections.emptyList());

        // Act
        MemeResponse response = memeService.createMeme(memeRequest, mockFile);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Meme", response.getTitle());
        assertEquals("test-meme.jpg", response.getUrl());
        assertEquals(userSummary, response.getUser());
        assertEquals(categoryDtos, response.getCategories());
        assertEquals(0L, response.getVoteCount());
        assertFalse(response.getUserVoted());

        // Verify
        verify(userService).getCurrentUser();
        verify(imageStorageService).storeImage(mockFile);
        verify(categoryService).getCategoriesByNames(memeRequest.getCategories());
        verify(memeRepository).save(any(Meme.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/memes"), any(Object.class));
    }

    @Test
    void getMemeById_ShouldReturnMemeResponse() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(memeRepository.findById(1L)).thenReturn(Optional.of(testMeme));
        when(userService.getUserSummary(any(User.class))).thenReturn(userSummary);
        when(categoryService.mapToCategoryDtoSet(anySet())).thenReturn(categoryDtos);
        when(voteRepository.countByMemeId(anyLong())).thenReturn(5L);
        when(voteRepository.existsByUserAndMeme(any(User.class), any(Meme.class))).thenReturn(true);

        List<User> voters = new ArrayList<>();
        voters.add(testUser);
        when(voteRepository.findVotersByMemeId(anyLong())).thenReturn(voters);

        // Act
        MemeResponse response = memeService.getMemeById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Meme", response.getTitle());
        assertEquals("test-meme.jpg", response.getUrl());
        assertEquals(userSummary, response.getUser());
        assertEquals(categoryDtos, response.getCategories());
        assertEquals(5L, response.getVoteCount());
        assertTrue(response.getUserVoted());
        assertEquals(1, response.getVoters().size());

        // Verify
        verify(memeRepository).findById(1L);
        verify(userService).getUserSummary(testUser);
        verify(categoryService).mapToCategoryDtoSet(categories);
        verify(voteRepository).countByMemeId(1L);
        verify(voteRepository).existsByUserAndMeme(testUser, testMeme);
        verify(voteRepository).findVotersByMemeId(1L);
    }

    @Test
    void getMemes_ShouldReturnPageOfMemeResponses() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Meme> memes = Collections.singletonList(testMeme);
        Page<Meme> memePage = new PageImpl<>(memes, pageable, 1);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(memeRepository.findAll(pageable)).thenReturn(memePage);
        when(userService.getUserSummary(any(User.class))).thenReturn(userSummary);
        when(categoryService.mapToCategoryDtoSet(anySet())).thenReturn(categoryDtos);
        when(voteRepository.countByMemeId(anyLong())).thenReturn(5L);
        when(voteRepository.existsByUserAndMeme(any(User.class), any(Meme.class))).thenReturn(true);
        when(voteRepository.findVotersByMemeId(anyLong())).thenReturn(Collections.singletonList(testUser));

        // Act
        Page<MemeResponse> responsePage = memeService.getMemes(null, null, null, pageable);

        // Assert
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        MemeResponse response = responsePage.getContent().get(0);
        assertEquals(1L, response.getId());
        assertEquals("Test Meme", response.getTitle());

        // Verify
        verify(memeRepository).findAll(pageable);
    }
}
