package com.taskmanager.org.restControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.org.controller.TaskController;
import com.taskmanager.org.dto.TaskDTO;
import com.taskmanager.org.model.*;
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
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.taskmanager.org.exception.GlobalExceptionHandler.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserService userService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void shouldReturnAllTasks() throws Exception {
        User user = new User();
        user.setId(1L);

        Task task = new Task(
                1L, "Learn", "Learn for exam", Status.TO_DO,
                LocalDateTime.now().plusDays(1), null,
                LocalDateTime.now(), LocalDateTime.now(), user
        );

        List<Task> mockTasks = List.of(task);

        when(taskService.findAllTasks()).thenReturn(mockTasks);

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Learn"))
                .andExpect(jsonPath("$[0].description").value("Learn for exam"))
                .andExpect(jsonPath("$[0].status").value("TO_DO"));

        verify(taskService).findAllTasks();
    }

    @Test
    void shouldReturnSpecificTask() throws Exception {
        User user = new User();
        user.setId(2L);

        Task task = new Task(
                2L, "Task 2", "Details", Status.IN_PROGRESS,
                LocalDateTime.now().plusDays(2), null,
                LocalDateTime.now(), LocalDateTime.now(), user
        );

        when(taskService.findTaskById(2L)).thenReturn(List.of(task));

        mockMvc.perform(get("/api/v1/tasks/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.title").value("Task 2"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldReturnNotFoundForMissingTask() throws Exception {
        when(taskService.findTaskById(99L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/v1/tasks/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewTask() throws Exception {
        TaskDTO request = new TaskDTO(
                null, "New Task", "Desc", Status.TO_DO,
                LocalDateTime.now().plusDays(3), null,
                null, null, 5L
        );

        User mockUser = new User();
        mockUser.setId(5L);

        String json = objectMapper.writeValueAsString(request);

        when(userService.findById(5L)).thenReturn(Optional.of(mockUser));

        when(taskService.addNewTask(eq(mockUser), any(Task.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.userId").value(5L));

        verify(taskService).addNewTask(eq(mockUser), any(Task.class));
    }

    @Test
    void shouldRejectNullUserId() throws Exception {
        TaskDTO request = new TaskDTO(
                null, "Test", "Desc", Status.TO_DO,
                LocalDateTime.now().plusDays(3), null,
                null, null, null
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).addNewTask(any(), any());
    }

    @Test
    void shouldDeleteTask() throws Exception {
        Task task = new Task();
        task.setId(3L);

        when(taskService.findTaskById(3L)).thenReturn(List.of(task));
        doNothing().when(taskService).removeTask(task);

        mockMvc.perform(delete("/api/v1/tasks/{id}", 3L))
                .andExpect(status().isNoContent());

        verify(taskService).removeTask(task);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingTask() throws Exception {
        when(taskService.findTaskById(100L)).thenReturn(new ArrayList<>());

        mockMvc.perform(delete("/api/v1/tasks/{id}", 100L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateTask() throws Exception {
        User user = new User();
        user.setId(1L);

        Task existingTask = new Task(
                10L, "Old title", "Old desc", Status.TO_DO,
                LocalDateTime.now().plusDays(1), null,
                LocalDateTime.now(), LocalDateTime.now(), user
        );

        TaskDTO request = new TaskDTO(
                10L, "New title", "New desc", Status.IN_PROGRESS,
                LocalDateTime.now().plusDays(5), null,
                null, null, 1L
        );

        when(taskService.findTaskById(10L)).thenReturn(List.of(existingTask));

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/tasks/{id}", 10L)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
    @Test
    void shouldExportAllTasksAsCsv() throws Exception {

        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setId(10L);
        category.setName("Work");

        Task task = new Task(
                1L, "Task A", "Desc A", Status.TO_DO,
                LocalDateTime.of(2025, 1, 10, 10, 0),
                category,
                LocalDateTime.of(2025, 1, 1, 12, 0),
                LocalDateTime.of(2025, 1, 2, 12, 0),
                user
        );

        when(taskService.findAllTasks()).thenReturn(List.of(task));


        mockMvc.perform(get("/api/v1/tasks/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"tasks.csv\""))
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Id,Title,Description,Status,DueDate,Category,CreatedAt,UpdatedAt")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Task A")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Work")));

        verify(taskService).findAllTasks();
    }
    @Test
    void shouldExportUserTasksAsCsv() throws Exception {
        User user = new User();
        user.setId(5L);

        Category category = new Category();
        category.setId(2L);
        category.setName("Home");

        Task task = new Task(
                3L, "Clean", "Clean room", Status.IN_PROGRESS,
                LocalDateTime.of(2025, 2, 15, 8, 0),
                category,
                LocalDateTime.of(2025, 2, 1, 9, 0),
                LocalDateTime.of(2025, 2, 1, 10, 0),
                user
        );

        when(userService.findById(5L)).thenReturn(Optional.of(user));
        when(taskService.findTaskByUser(user)).thenReturn(List.of(task));

        mockMvc.perform(get("/api/v1/tasks/export/csv")
                        .param("userId", "5"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"tasks_5.csv\""))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Clean")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Home")));

        verify(taskService).findTaskByUser(user);
    }
    @Test
    void shouldReturnCorrectHeadersForCsvDownload() throws Exception {
        when(taskService.findAllTasks()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/tasks/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"tasks.csv\""));
    }



}

