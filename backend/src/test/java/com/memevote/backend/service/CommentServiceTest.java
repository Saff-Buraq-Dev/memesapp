package com.memevote.backend.service;

import com.memevote.backend.dto.request.CommentRequest;
import com.memevote.backend.dto.response.CommentResponse;
import com.memevote.backend.dto.response.UserSummary;
import com.memevote.backend.dto.websocket.WebSocketEvent;
import com.memevote.backend.model.Comment;
import com.memevote.backend.model.Meme;
import com.memevote.backend.model.User;
import com.memevote.backend.repository.CommentRepository;
import com.memevote.backend.repository.MemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.core.MessagePostProcessor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemeRepository memeRepository;

    @Mock
    private UserService userService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Meme testMeme;
    private Comment testComment;
    private CommentRequest commentRequest;
    private UserSummary userSummary;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setProfilePicture("profile.jpg");

        // Setup test meme
        testMeme = new Meme();
        testMeme.setId(1L);
        testMeme.setTitle("Test Meme");
        testMeme.setUrl("test-meme.jpg");
        testMeme.setUser(testUser);

        // Setup test comment
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setText("This is a test comment");
        testComment.setUser(testUser);
        testComment.setMeme(testMeme);
        testComment.setCreatedAt(LocalDateTime.now());

        // Setup comment request
        commentRequest = new CommentRequest();
        commentRequest.setText("This is a test comment");

        // Setup UserSummary
        userSummary = new UserSummary();
        userSummary.setId(1L);
        userSummary.setUsername("testuser");
        userSummary.setProfilePicture("profile.jpg");
    }

    @Test
    void addComment_ShouldReturnCommentResponse() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(memeRepository.findById(1L)).thenReturn(Optional.of(testMeme));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(userService.getUserSummary(testUser)).thenReturn(userSummary);

        // Act
        CommentResponse response = commentService.addComment(1L, commentRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("This is a test comment", response.getText());
        assertEquals(userSummary, response.getUser());
        assertEquals(1L, response.getMemeId());

        // Verify
        verify(userService).getCurrentUser();
        verify(memeRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
        verify(userService).getUserSummary(testUser);

        // Verify WebSocket message
        ArgumentCaptor<WebSocketEvent> eventCaptor = ArgumentCaptor.forClass(WebSocketEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/memes/1/comments"), eventCaptor.capture());

        WebSocketEvent capturedEvent = eventCaptor.getValue();
        assertEquals("NEW_COMMENT", capturedEvent.getType());

        CommentResponse payload = (CommentResponse) capturedEvent.getPayload();
        assertEquals(1L, payload.getId());
        assertEquals("This is a test comment", payload.getText());
    }

    @Test
    void getCommentsByMemeId_ShouldReturnPageOfCommentResponses() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Comment> comments = Arrays.asList(testComment);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, 1);

        when(memeRepository.findById(1L)).thenReturn(Optional.of(testMeme));
        when(commentRepository.findByMeme(testMeme, pageable)).thenReturn(commentPage);
        when(userService.getUserSummary(testUser)).thenReturn(userSummary);

        // Act
        Page<CommentResponse> responsePage = commentService.getCommentsByMemeId(1L, pageable);

        // Assert
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());

        CommentResponse response = responsePage.getContent().get(0);
        assertEquals(1L, response.getId());
        assertEquals("This is a test comment", response.getText());
        assertEquals(userSummary, response.getUser());
        assertEquals(1L, response.getMemeId());

        // Verify
        verify(memeRepository).findById(1L);
        verify(commentRepository).findByMeme(testMeme, pageable);
        verify(userService).getUserSummary(testUser);
    }

    @Test
    void getRecentCommentsByMemeId_ShouldReturnListOfCommentResponses() {
        // Arrange
        List<Comment> comments = Arrays.asList(testComment);

        when(memeRepository.findById(1L)).thenReturn(Optional.of(testMeme));
        when(commentRepository.findByMemeOrderByCreatedAtDesc(testMeme)).thenReturn(comments);
        when(userService.getUserSummary(testUser)).thenReturn(userSummary);

        // Act
        List<CommentResponse> responseList = commentService.getRecentCommentsByMemeId(1L);

        // Assert
        assertNotNull(responseList);
        assertEquals(1, responseList.size());

        CommentResponse response = responseList.get(0);
        assertEquals(1L, response.getId());
        assertEquals("This is a test comment", response.getText());
        assertEquals(userSummary, response.getUser());
        assertEquals(1L, response.getMemeId());

        // Verify
        verify(memeRepository).findById(1L);
        verify(commentRepository).findByMemeOrderByCreatedAtDesc(testMeme);
        verify(userService).getUserSummary(testUser);
    }

    @Test
    void addComment_WhenMemeNotFound_ShouldThrowException() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(memeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            commentService.addComment(1L, commentRequest);
        });

        assertEquals("Meme not found", exception.getMessage());

        // Verify
        verify(userService).getCurrentUser();
        verify(memeRepository).findById(1L);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
}
