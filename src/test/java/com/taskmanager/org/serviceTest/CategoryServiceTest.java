package com.taskmanager.org.serviceTest;

import com.taskmanager.org.model.Category;
import com.taskmanager.org.repository.CategoryRepository;
import com.taskmanager.org.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        category = new Category();
        category.setId(1L);
        category.setName("Work");
    }

    @Test
    void testAddNewCategory() {
        when(categoryRepository.save(category)).thenReturn(category);

        Category savedCategory = categoryService.addNewCategory(category);

        assertEquals(category, savedCategory);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void testRemoveCategory() {
        categoryService.removeCategory(category);
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void testFindById() {
        List<Category> categories = new ArrayList<>();
        categories.add(category);

        when(categoryRepository.findCategoryById(1L)).thenReturn(categories);

        List<Category> result = categoryService.findById(1L);

        assertEquals(1, result.size());
        assertEquals(category, result.get(0));
    }

    @Test
    void testFindAllCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(category);

        when(categoryRepository.findAll()).thenReturn(categories);

        List<Category> result = categoryService.findAllCategories();

        assertEquals(1, result.size());
        assertEquals(category, result.get(0));
    }
}
