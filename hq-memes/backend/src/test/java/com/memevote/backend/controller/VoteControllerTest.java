package com.memevote.backend.controller;

import com.memevote.backend.dto.response.MessageResponse;
import com.memevote.backend.service.VoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class VoteControllerTest {

    @Mock
    private VoteService voteService;

    @InjectMocks
    private VoteController voteController;

    private MockMvc mockMvc;
    private MessageResponse voteAddedResponse;
    private MessageResponse voteRemovedResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(voteController).build();
        
        // Setup message responses
        voteAddedResponse = new MessageResponse("Vote added successfully");
        voteRemovedResponse = new MessageResponse("Vote removed successfully");
    }

    @Test
    void toggleVote_WhenVoteAdded_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        when(voteService.toggleVote(1L)).thenReturn(voteAddedResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/memes/1/votes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vote added successfully"));
    }

    @Test
    void toggleVote_WhenVoteRemoved_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        when(voteService.toggleVote(1L)).thenReturn(voteRemovedResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/memes/1/votes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vote removed successfully"));
    }
}
