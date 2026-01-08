package com.taskmanager.org.mvcControllerTest;

import com.taskmanager.org.controller.TaskViewController;
import com.taskmanager.org.dto.TaskViewDTO;
import com.taskmanager.org.exception.CategoryNotFoundException;
import com.taskmanager.org.model.*;
import com.taskmanager.org.service.*;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.within;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskViewController.class)
class TaskViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private TaskViewController controller;


    @BeforeEach
    void setup() throws Exception {
        controller = new TaskViewController(taskService, categoryService, userService);
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter)
                .doFilter(any(), any(), any());
    }

    private User mockUser() {
        User user = new User();
        user.setEmail("test@example.com");
        return user;
    }

    private Task mockTask() {
        Task t = new Task();
        t.setId(1L);
        t.setTitle("Task A");
        t.setDescription("Desc");
        t.setStatus(Status.TO_DO);
        t.setCategoryId(new Category(1L, "Work", "Blue"));
        t.setDueDate(LocalDateTime.now().plusDays(1));
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        return t;
    }

    @TestConfiguration
    static class ConverterConfig {
        @Bean
        public StringToCategoryConverter stringToCategoryConverter(CategoryService categoryService) {
            return new StringToCategoryConverter(categoryService);
        }
    }

    @Component
    public static class StringToCategoryConverter implements Converter<String, Category> {
        private final CategoryService categoryService;

        public StringToCategoryConverter(CategoryService categoryService) {
            this.categoryService = categoryService;
        }

        @Override
        public Category convert(String source) {
            List<Category> category = categoryService.findById(Long.valueOf(source));
            if (category.isEmpty()) {
                throw new CategoryNotFoundException("Category not found");
            }
            return category.get(0);
        }
    }



    @Test
    @WithMockUser(username = "test@example.com")
    void tasksPage_ShouldReturnView() throws Exception {
        User user = mockUser();
        when(userService.findByEmail("test@example.com"))
                .thenReturn(List.of(user));

        Task task = mockTask();
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskService.findTasksFiltered(eq(user), any(), any(), any(), any()))
                .thenReturn(page);

        when(categoryService.findAllCategories())
                .thenReturn(List.of(new Category(1L, "Work", "Blue")));

        when(taskService.findTaskByUser(user))
                .thenReturn(List.of(task));

        MvcResult result = mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andReturn();

        assertThat(result.getModelAndView().getModel().get("tasks")).isNotNull();
    }


    @Test
    @WithMockUser
    void showCreateForm_ShouldReturnForm() throws Exception {
        when(categoryService.findAllCategories())
                .thenReturn(List.of(new Category(1L, "Work", "Blue")));

        mockMvc.perform(get("/tasks/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attributeExists("categories"));
    }


    @Test
    @WithMockUser(username = "test@example.com")
    void createTask_ShouldSaveTask_AndRedirect() throws Exception {
        User user = mockUser();
        when(userService.findByEmail("test@example.com"))
                .thenReturn(List.of(user));

        when(categoryService.findAllCategories())
                .thenReturn(List.of(new Category(1L, "Work", "Blue")));

        Category category = new Category(1L, "Work", "Blue");
        when(categoryService.findById(1L)).thenReturn(List.of(category));

        mockMvc.perform(post("/tasks/add")
                        .param("title", "New Task")
                        .param("description", "Some desc")
                        .param("category","1")
                        .param("dueDate", LocalDateTime.now().plusDays(1).toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        verify(taskService, times(1))
                .addNewTask(eq(user), any(Task.class));
    }



    @Test
    @WithMockUser
    void deleteTask_ShouldRemoveTask() throws Exception {
        Task task = mockTask();
        when(taskService.findTaskById(1L)).thenReturn(List.of(task));

        mockMvc.perform(post("/tasks/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        verify(taskService).removeTask(task);
    }


    @Test
    @WithMockUser
    void editForm_ShouldReturnView() throws Exception {
        Task task = mockTask();
        when(taskService.findTaskById(1L)).thenReturn(List.of(task));
        when(categoryService.findAllCategories())
                .thenReturn(List.of(new Category(1L, "Work", "Blue")));

        mockMvc.perform(get("/tasks/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/editForm"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attributeExists("categories"));
    }


    @Test
    @WithMockUser
    void changeTask_ShouldUpdateAndRedirect() throws Exception {
        Task task = mockTask();
        when(taskService.findTaskById(1L)).thenReturn(List.of(task));
        String dueDateStr = LocalDateTime.now().plusDays(2)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        Category category = new Category(1L, "Work", "Blue");
        when(categoryService.findById(1L)).thenReturn(List.of(category));
        mockMvc.perform(post("/tasks/edit/1")
                        .param("title", "Updated")
                        .param("description", "Updated desc")
                        .param("category", "1")
                        .param("status", "IN_PROGRESS")
                        .param("dueDate", dueDateStr)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        verify(taskService).save(any(Task.class));
    }


    @Test
    @WithMockUser
    void changeTask_ShouldFailValidation() throws Exception {
        String dueDateStr = LocalDateTime.now().plusDays(2)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        Category category = new Category(1L, "Work", "Blue");
        when(categoryService.findById(1L)).thenReturn(List.of(category));
        mockMvc.perform(post("/tasks/edit/1")
                        .param("title", "")
                        .param("description", "desc")
                        .param("category", "1")
                        .param("status", "TO_DO")
                        .param("dueDate", dueDateStr)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/edit/1"))
                .andExpect(flash().attributeExists("error"));
    }
    @Test
    @WithMockUser(username = "test@example.com")
    void createTask_ShouldFail_WhenTitleIsEmpty() throws Exception {
        when(categoryService.findById(1L)).thenReturn(List.of(new Category(1L, "Work", "Blue")));

        mockMvc.perform(post("/tasks/add")
                        .param("title", "")
                        .param("description", "Some desc")
                        .param("category","1")
                        .param("dueDate", LocalDateTime.now().plusDays(1).toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/add"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createTask_ShouldFail_WhenDescriptionIsEmpty() throws Exception {
        when(categoryService.findById(1L)).thenReturn(List.of(new Category(1L, "Work", "Blue")));

        mockMvc.perform(post("/tasks/add")
                        .param("title", "A")
                        .param("description", "")
                        .param("category", "1")
                        .param("dueDate", LocalDateTime.now().plusDays(1).toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/add"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void changeTask_ShouldFailValidation_WhenTitleEmpty() throws Exception {
        String dueDateStr = LocalDateTime.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        Category category = new Category(1L, "Work", "Blue");
        when(categoryService.findById(1L)).thenReturn(List.of(category));

        mockMvc.perform(post("/tasks/edit/1")
                        .param("title", "")
                        .param("description", "Valid description")
                        .param("category", "1")
                        .param("status", "TO_DO")
                        .param("dueDate", dueDateStr)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/edit/1"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void changeTask_ShouldFailValidation_WhenDescriptionEmpty() throws Exception {
        String dueDateStr = LocalDateTime.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        Category category = new Category(1L, "Work", "Blue");
        when(categoryService.findById(1L)).thenReturn(List.of(category));

        mockMvc.perform(post("/tasks/edit/1")
                        .param("title", "Valid title")
                        .param("description", "")
                        .param("category", "1")
                        .param("status", "TO_DO")
                        .param("dueDate", dueDateStr)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/edit/1"))
                .andExpect(flash().attributeExists("error"));
    }




    @Test
    void calculateDonePercentage_ShouldReturn0_WhenCreatedAtOrDueDateNull() {
        assertThat(controller.calculateDonePercentage(null, LocalDateTime.now())).isEqualTo(0f);
        assertThat(controller.calculateDonePercentage(LocalDateTime.now(), null)).isEqualTo(0f);
        assertThat(controller.calculateDonePercentage(null, null)).isEqualTo(0f);
    }

    @Test
    void calculateDonePercentage_ShouldReturn0_WhenDueDateBeforeCreatedAt() {
        LocalDateTime created = LocalDateTime.now().plusDays(1);
        LocalDateTime due = LocalDateTime.now();
        assertThat(controller.calculateDonePercentage(created, due)).isEqualTo(0f);
    }

    @Test
    void calculateDonePercentage_ShouldReturn0_WhenNowBeforeCreatedAt() {
        LocalDateTime created = LocalDateTime.now().plusDays(1);
        LocalDateTime due = LocalDateTime.now().plusDays(2);
        assertThat(controller.calculateDonePercentage(created, due)).isEqualTo(0f);
    }

    @Test
    void calculateDonePercentage_ShouldReturn100_WhenNowAfterDueDate() {
        LocalDateTime created = LocalDateTime.now().minusDays(2);
        LocalDateTime due = LocalDateTime.now().minusDays(1);
        assertThat(controller.calculateDonePercentage(created, due)).isEqualTo(100f);
    }

    @Test
    void calculateDonePercentage_ShouldReturn50_WhenHalfTimePassed() {
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime due = LocalDateTime.now().plusDays(1);
        float percentage = controller.calculateDonePercentage(created, due);
        assertThat(percentage).isCloseTo(50f, within(1f));
    }

    @Test
    void calculateDonePercentage_ShouldReturn0_WhenNowEqualCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(controller.calculateDonePercentage(now, now.plusDays(1))).isCloseTo(0f, within(0.1f));
    }





}





