package com.memevote.backend.service;

import com.memevote.backend.dto.response.MessageResponse;
import com.memevote.backend.dto.websocket.WebSocketEvent;
import com.memevote.backend.model.Meme;
import com.memevote.backend.model.User;
import com.memevote.backend.model.Vote;
import com.memevote.backend.repository.MemeRepository;
import com.memevote.backend.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class VoteService {
    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public MessageResponse toggleVote(Long memeId) {
        User currentUser = userService.getCurrentUser();

        Meme meme = memeRepository.findById(memeId)
                .orElseThrow(() -> new RuntimeException("Meme not found"));

        Optional<Vote> existingVote = voteRepository.findByUserAndMeme(currentUser, meme);

        if (existingVote.isPresent()) {
            // Remove vote
            voteRepository.delete(existingVote.get());

            // Send WebSocket update
            Long newVoteCount = voteRepository.countByMemeId(memeId);
            Map<String, Object> payload = new HashMap<>();
            payload.put("memeId", memeId);
            payload.put("voteCount", newVoteCount);
            payload.put("userVoted", false);

            WebSocketEvent<Map<String, Object>> event = new WebSocketEvent<>();
            event.setType("VOTE_UPDATED");
            event.setPayload(payload);
            messagingTemplate.convertAndSend("/topic/memes/" + memeId + "/votes", event);

            return new MessageResponse("Vote removed successfully");
        } else {
            // Add vote
            Vote vote = new Vote();
            vote.setUser(currentUser);
            vote.setMeme(meme);
            voteRepository.save(vote);

            // Send WebSocket update
            Long newVoteCount = voteRepository.countByMemeId(memeId);
            Map<String, Object> payload = new HashMap<>();
            payload.put("memeId", memeId);
            payload.put("voteCount", newVoteCount);
            payload.put("userVoted", true);

            WebSocketEvent<Map<String, Object>> event = new WebSocketEvent<>();
            event.setType("VOTE_UPDATED");
            event.setPayload(payload);
            messagingTemplate.convertAndSend("/topic/memes/" + memeId + "/votes", event);

            return new MessageResponse("Vote added successfully");
        }
    }
}
