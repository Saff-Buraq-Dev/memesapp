package com.memevote.backend.service;

import com.memevote.backend.dto.response.CategoryDto;
import com.memevote.backend.model.Category;
import com.memevote.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    public Set<Category> getCategoriesByNames(Set<String> categoryNames) {
        Set<Category> existingCategories = categoryRepository.findByNameIn(categoryNames);
        Set<String> existingCategoryNames = existingCategories.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());

        // Create new categories for names that don't exist yet
        Set<Category> newCategories = new HashSet<>();
        for (String name : categoryNames) {
            if (!existingCategoryNames.contains(name)) {
                Category newCategory = new Category();
                newCategory.setName(name);
                newCategories.add(categoryRepository.save(newCategory));
            }
        }

        // Combine existing and new categories
        existingCategories.addAll(newCategories);
        return existingCategories;
    }

    public CategoryDto mapToCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

    public Set<CategoryDto> mapToCategoryDtoSet(Set<Category> categories) {
        return categories.stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toSet());
    }
}
