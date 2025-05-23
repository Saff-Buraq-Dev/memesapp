package com.memevote.backend.service;

import com.memevote.backend.dto.request.CommentRequest;
import com.memevote.backend.dto.response.CommentResponse;
import com.memevote.backend.dto.websocket.WebSocketEvent;
import com.memevote.backend.model.Comment;
import com.memevote.backend.model.Meme;
import com.memevote.backend.model.User;
import com.memevote.backend.repository.CommentRepository;
import com.memevote.backend.repository.MemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public CommentResponse addComment(Long memeId, CommentRequest commentRequest) {
        User currentUser = userService.getCurrentUser();

        Meme meme = memeRepository.findById(memeId)
                .orElseThrow(() -> new RuntimeException("Meme not found"));

        Comment comment = new Comment();
        comment.setText(commentRequest.getText());
        comment.setUser(currentUser);
        comment.setMeme(meme);

        comment = commentRepository.save(comment);

        CommentResponse commentResponse = mapToCommentResponse(comment);

        // Send WebSocket update
        WebSocketEvent<CommentResponse> event = new WebSocketEvent<>();
        event.setType("NEW_COMMENT");
        event.setPayload(commentResponse);
        messagingTemplate.convertAndSend("/topic/memes/" + memeId + "/comments", event);

        return commentResponse;
    }

    public Page<CommentResponse> getCommentsByMemeId(Long memeId, Pageable pageable) {
        Meme meme = memeRepository.findById(memeId)
                .orElseThrow(() -> new RuntimeException("Meme not found"));

        return commentRepository.findByMeme(meme, pageable)
                .map(this::mapToCommentResponse);
    }

    public List<CommentResponse> getRecentCommentsByMemeId(Long memeId) {
        Meme meme = memeRepository.findById(memeId)
                .orElseThrow(() -> new RuntimeException("Meme not found"));

        return commentRepository.findByMemeOrderByCreatedAtDesc(meme).stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getCreatedAt(),
                userService.getUserSummary(comment.getUser()),
                comment.getMeme().getId()
        );
    }
}
