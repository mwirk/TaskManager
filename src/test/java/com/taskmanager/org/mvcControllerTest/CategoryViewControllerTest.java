package com.taskmanager.org.mvcControllerTest;

import com.taskmanager.org.controller.CategoryViewController;
import com.taskmanager.org.model.Category;
import com.taskmanager.org.service.*;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryViewController.class)
class CategoryViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setup() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter)
                .doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser
    void showCreateForm_ShouldReturnFormView() throws Exception {
        mockMvc.perform(get("/categories/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"))
                .andExpect(model().attributeExists("category"));
    }


    @Test
    @WithMockUser
    void createCategory_ShouldAddCategoryAndRedirect() throws Exception {
        mockMvc.perform(post("/categories/add")
                        .param("name", "Work")
                        .param("color", "Blue")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/add"))
                .andExpect(flash().attributeExists("success"));



        verify(categoryService, times(1)).addNewCategory(any(Category.class));
    }


    @Test
    @WithMockUser
    void createCategory_ShouldFail_WhenNameMissing() throws Exception {
        mockMvc.perform(post("/categories/add")
                        .param("name", "")
                        .param("color", "Blue")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories/add"))
                .andExpect(flash().attributeExists("error"));

        verify(categoryService, never()).addNewCategory(any(Category.class));
    }

    @Test
    @WithMockUser
    void createCategory_ShouldFail_WhenColorMissing() throws Exception {
        mockMvc.perform(post("/categories/add")
                        .param("name", "Work")
                        .param("color", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories/add"))
                .andExpect(flash().attributeExists("error"));

        verify(categoryService, never()).addNewCategory(any(Category.class));
    }

    @Test
    @WithMockUser
    void createCategory_ShouldFail_WhenNameAndColorMissing() throws Exception {
        mockMvc.perform(post("/categories/add")
                        .param("name", "")
                        .param("color", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories/add"))
                .andExpect(flash().attributeExists("error"));

        verify(categoryService, never()).addNewCategory(any(Category.class));
    }


}
