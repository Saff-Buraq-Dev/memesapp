package com.memevote.backend.repository;

import com.memevote.backend.model.Category;
import com.memevote.backend.model.Meme;
import com.memevote.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MemeRepository extends JpaRepository<Meme, Long> {
    Page<Meme> findByUser(User user, Pageable pageable);
    
    Page<Meme> findByCategoriesIn(Set<Category> categories, Pageable pageable);
    
    Page<Meme> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    @Query("SELECT m FROM Meme m JOIN m.categories c WHERE c IN :categories AND m.user = :user")
    Page<Meme> findByCategoriesInAndUser(Set<Category> categories, User user, Pageable pageable);
    
    @Query("SELECT m FROM Meme m JOIN m.categories c WHERE c IN :categories AND m.title LIKE %:title%")
    Page<Meme> findByCategoriesInAndTitleContainingIgnoreCase(Set<Category> categories, String title, Pageable pageable);
    
    @Query("SELECT m FROM Meme m WHERE m.user = :user AND m.title LIKE %:title%")
    Page<Meme> findByUserAndTitleContainingIgnoreCase(User user, String title, Pageable pageable);
    
    @Query("SELECT m FROM Meme m JOIN m.categories c WHERE c IN :categories AND m.user = :user AND m.title LIKE %:title%")
    Page<Meme> findByCategoriesInAndUserAndTitleContainingIgnoreCase(Set<Category> categories, User user, String title, Pageable pageable);
}
