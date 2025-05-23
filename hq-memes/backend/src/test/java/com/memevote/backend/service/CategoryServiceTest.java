package com.memevote.backend.service;

import com.memevote.backend.dto.response.CategoryDto;
import com.memevote.backend.model.Category;
import com.memevote.backend.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private List<Category> categories;
    private Category funnyCategory;
    private Category memesCategory;

    @BeforeEach
    void setUp() {
        // Setup test categories
        funnyCategory = new Category();
        funnyCategory.setId(1L);
        funnyCategory.setName("Funny");

        memesCategory = new Category();
        memesCategory.setId(2L);
        memesCategory.setName("Memes");

        categories = Arrays.asList(funnyCategory, memesCategory);
    }

    @Test
    void getAllCategories_ShouldReturnAllCategoriesAsDtos() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(categories);

        // Act
        List<CategoryDto> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        CategoryDto funnyDto = result.stream()
                .filter(dto -> dto.getId().equals(1L))
                .findFirst()
                .orElse(null);
        
        assertNotNull(funnyDto);
        assertEquals("Funny", funnyDto.getName());
        
        CategoryDto memesDto = result.stream()
                .filter(dto -> dto.getId().equals(2L))
                .findFirst()
                .orElse(null);
        
        assertNotNull(memesDto);
        assertEquals("Memes", memesDto.getName());
        
        // Verify
        verify(categoryRepository).findAll();
    }

    @Test
    void getCategoriesByNames_WithExistingCategories_ShouldReturnExistingCategories() {
        // Arrange
        Set<String> categoryNames = new HashSet<>(Arrays.asList("Funny", "Memes"));
        Set<Category> existingCategories = new HashSet<>(categories);
        
        when(categoryRepository.findByNameIn(categoryNames)).thenReturn(existingCategories);

        // Act
        Set<Category> result = categoryService.getCategoriesByNames(categoryNames);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        Set<String> resultNames = result.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
        
        assertTrue(resultNames.contains("Funny"));
        assertTrue(resultNames.contains("Memes"));
        
        // Verify
        verify(categoryRepository).findByNameIn(categoryNames);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategoriesByNames_WithNewCategories_ShouldCreateAndReturnAllCategories() {
        // Arrange
        Set<String> categoryNames = new HashSet<>(Arrays.asList("Funny", "Memes", "New"));
        Set<Category> existingCategories = new HashSet<>(categories);
        
        Category newCategory = new Category();
        newCategory.setId(3L);
        newCategory.setName("New");
        
        when(categoryRepository.findByNameIn(categoryNames)).thenReturn(existingCategories);
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);

        // Act
        Set<Category> result = categoryService.getCategoriesByNames(categoryNames);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        
        Set<String> resultNames = result.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
        
        assertTrue(resultNames.contains("Funny"));
        assertTrue(resultNames.contains("Memes"));
        assertTrue(resultNames.contains("New"));
        
        // Verify
        verify(categoryRepository).findByNameIn(categoryNames);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void mapToCategoryDto_ShouldMapCategoryToDto() {
        // Act
        CategoryDto result = categoryService.mapToCategoryDto(funnyCategory);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Funny", result.getName());
    }

    @Test
    void mapToCategoryDtoSet_ShouldMapCategoriesToDtos() {
        // Arrange
        Set<Category> categorySet = new HashSet<>(categories);

        // Act
        Set<CategoryDto> result = categoryService.mapToCategoryDtoSet(categorySet);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        boolean hasFunny = result.stream()
                .anyMatch(dto -> dto.getId().equals(1L) && dto.getName().equals("Funny"));
        
        boolean hasMemes = result.stream()
                .anyMatch(dto -> dto.getId().equals(2L) && dto.getName().equals("Memes"));
        
        assertTrue(hasFunny);
        assertTrue(hasMemes);
    }
}
