package com.memevote.backend.repository;

import com.memevote.backend.model.Meme;
import com.memevote.backend.model.User;
import com.memevote.backend.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserAndMeme(User user, Meme meme);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.meme.id = :memeId")
    Long countByMemeId(Long memeId);

    Boolean existsByUserAndMeme(User user, Meme meme);

    @Query("SELECT v.user FROM Vote v WHERE v.meme.id = :memeId")
    List<User> findVotersByMemeId(Long memeId);
}
