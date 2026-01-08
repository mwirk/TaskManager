package com.taskmanager.org.restControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.org.controller.CategoryController;
import com.taskmanager.org.dto.CategoryDTO;
import com.taskmanager.org.model.Category;
import com.taskmanager.org.service.CategoryService;
import com.taskmanager.org.service.JwtService;
import com.taskmanager.org.service.TaskService;
import com.taskmanager.org.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.taskmanager.org.exception.GlobalExceptionHandler.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserService userService;

    @Test
    void shouldReturnAllCategories() throws Exception {
        Category category = new Category(1L, "Work", "Red");
        when(categoryService.findAllCategories()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Work"))
                .andExpect(jsonPath("$[0].color").value("Red"));

        verify(categoryService).findAllCategories();
    }

    @Test
    void shouldReturnSpecificCategory() throws Exception {
        Category category = new Category(2L, "Home", "Blue");
        when(categoryService.findById(2L)).thenReturn(List.of(category));

        mockMvc.perform(get("/api/v1/categories/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Home"))
                .andExpect(jsonPath("$[0].color").value("Blue"));
    }

    @Test
    void shouldReturnNotFoundForMissingCategory() throws Exception {
        when(categoryService.findById(99L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/v1/categories/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewCategory() throws Exception {
        CategoryDTO request = new CategoryDTO(null, "NewCat", "Green");
        Category savedCategory = new Category(5L, "NewCat", "Green");

        when(categoryService.addNewCategory(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(5L);
            return c;
        });

        mockMvc.perform(post("/api/v1/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewCat"))
                .andExpect(jsonPath("$.color").value("Green"))
                .andExpect(jsonPath("$.id").value(5L));

        verify(categoryService).addNewCategory(any(Category.class));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        Category category = new Category(3L, "Temp", "Yellow");
        when(categoryService.findById(3L)).thenReturn(List.of(category));
        doNothing().when(categoryService).removeCategory(category);

        mockMvc.perform(delete("/api/v1/categories/{id}", 3L))
                .andExpect(status().isNoContent());

        verify(categoryService).removeCategory(category);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingCategory() throws Exception {
        when(categoryService.findById(100L)).thenReturn(new ArrayList<>());

        mockMvc.perform(delete("/api/v1/categories/{id}", 100L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        Category category = new Category(10L, "OldName", "Black");
        when(categoryService.findById(10L)).thenReturn(List.of(category));

        CategoryDTO request = new CategoryDTO(10L, "UpdatedName", "White");

        mockMvc.perform(put("/api/v1/categories/{id}", 10L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedName"))
                .andExpect(jsonPath("$.color").value("White"));
    }
}
