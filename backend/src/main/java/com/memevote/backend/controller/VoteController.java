package com.memevote.backend.controller;

import com.memevote.backend.dto.response.MessageResponse;
import com.memevote.backend.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/memes/{memeId}/votes")
public class VoteController {
    @Autowired
    private VoteService voteService;

    @PostMapping
    public ResponseEntity<MessageResponse> toggleVote(@PathVariable Long memeId) {
        MessageResponse response = voteService.toggleVote(memeId);
        return ResponseEntity.ok(response);
    }
}
