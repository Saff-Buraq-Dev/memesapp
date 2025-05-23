package com.memevote.backend.repository;

import com.memevote.backend.model.Comment;
import com.memevote.backend.model.Meme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMemeOrderByCreatedAtDesc(Meme meme);
    Page<Comment> findByMeme(Meme meme, Pageable pageable);
}
