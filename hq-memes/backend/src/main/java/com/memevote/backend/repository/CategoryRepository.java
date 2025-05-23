package com.memevote.backend.repository;

import com.memevote.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    Set<Category> findByNameIn(Set<String> names);
    Boolean existsByName(String name);
}
