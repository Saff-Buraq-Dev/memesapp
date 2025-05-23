package com.memevote.backend.controller;

import com.memevote.backend.dto.request.CommentRequest;
import com.memevote.backend.dto.response.CommentResponse;
import com.memevote.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/memes/{memeId}/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long memeId,
            @Valid @RequestBody CommentRequest commentRequest) {
        CommentResponse commentResponse = commentService.addComment(memeId, commentRequest);
        return ResponseEntity.ok(commentResponse);
    }

    @GetMapping
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long memeId,
            Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentsByMemeId(memeId, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<CommentResponse>> getRecentComments(@PathVariable Long memeId) {
        List<CommentResponse> comments = commentService.getRecentCommentsByMemeId(memeId);
        return ResponseEntity.ok(comments);
    }
}
