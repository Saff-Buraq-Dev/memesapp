package com.memevote.backend.service;

import com.memevote.backend.dto.response.MessageResponse;
import com.memevote.backend.dto.websocket.WebSocketEvent;
import com.memevote.backend.model.Meme;
import com.memevote.backend.model.User;
import com.memevote.backend.model.Vote;
import com.memevote.backend.repository.MemeRepository;
import com.memevote.backend.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.core.MessagePostProcessor;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private MemeRepository memeRepository;

    @Mock
    private UserService userService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private VoteService voteService;

    private User testUser;
    private Meme testMeme;
    private Vote testVote;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Setup test meme
        testMeme = new Meme();
        testMeme.setId(1L);
        testMeme.setTitle("Test Meme");
        testMeme.setUrl("test-meme.jpg");
        testMeme.setUser(testUser);

        // Setup test vote
        testVote = new Vote();
        testVote.setId(1L);
        testVote.setUser(testUser);
        testVote.setMeme(testMeme);
    }

    @Test
    void toggleVote_WhenVoteDoesNotExist_ShouldAddVote() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(memeRepository.findById(1L)).thenReturn(Optional.of(testMeme));
        when(voteRepository.findByUserAndMeme(testUser, testMeme)).thenReturn(Optional.empty());
        when(voteRepository.countByMemeId(1L)).thenReturn(1L);

        // Act
        MessageResponse response = voteService.toggleVote(1L);

        // Assert
        assertNotNull(response);
        assertEquals("Vote added successfully", response.getMessage());

        // Verify
        verify(userService).getCurrentUser();
        verify(memeRepository).findById(1L);
        verify(voteRepository).findByUserAndMeme(testUser, testMeme);
        verify(voteRepository).save(any(Vote.class));
        verify(voteRepository).countByMemeId(1L);

        // Verify WebSocket message
        ArgumentCaptor<WebSocketEvent> eventCaptor = ArgumentCaptor.forClass(WebSocketEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/memes/1/votes"), eventCaptor.capture());

        WebSocketEvent capturedEvent = eventCaptor.getValue();
        assertEquals("VOTE_UPDATED", capturedEvent.getType());

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) capturedEvent.getPayload();
        assertEquals(1L, payload.get("memeId"));
        assertEquals(1L, payload.get("voteCount"));
        assertEquals(true, payload.get("userVoted"));
    }

    @Test
    void toggleVote_WhenVoteExists_ShouldRemoveVote() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(memeRepository.findById(1L)).thenReturn(Optional.of(testMeme));
        when(voteRepository.findByUserAndMeme(testUser, testMeme)).thenReturn(Optional.of(testVote));
        when(voteRepository.countByMemeId(1L)).thenReturn(0L);

        // Act
        MessageResponse response = voteService.toggleVote(1L);

        // Assert
        assertNotNull(response);
        assertEquals("Vote removed successfully", response.getMessage());

        // Verify
        verify(userService).getCurrentUser();
        verify(memeRepository).findById(1L);
        verify(voteRepository).findByUserAndMeme(testUser, testMeme);
        verify(voteRepository).delete(testVote);
        verify(voteRepository).countByMemeId(1L);

        // Verify WebSocket message
        ArgumentCaptor<WebSocketEvent> eventCaptor = ArgumentCaptor.forClass(WebSocketEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/memes/1/votes"), eventCaptor.capture());

        WebSocketEvent capturedEvent = eventCaptor.getValue();
        assertEquals("VOTE_UPDATED", capturedEvent.getType());

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) capturedEvent.getPayload();
        assertEquals(1L, payload.get("memeId"));
        assertEquals(0L, payload.get("voteCount"));
        assertEquals(false, payload.get("userVoted"));
    }

    @Test
    void toggleVote_WhenMemeNotFound_ShouldThrowException() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(memeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            voteService.toggleVote(1L);
        });

        assertEquals("Meme not found", exception.getMessage());

        // Verify
        verify(userService).getCurrentUser();
        verify(memeRepository).findById(1L);
        verify(voteRepository, never()).findByUserAndMeme(any(), any());
        verify(voteRepository, never()).save(any());
        verify(voteRepository, never()).delete(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
}
