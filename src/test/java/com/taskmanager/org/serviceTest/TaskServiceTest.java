package com.taskmanager.org.service;

import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Status;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.TaskRepository;
import com.taskmanager.org.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;
    private Category category;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setTasks(new ArrayList<>());

        category = new Category();
        category.setId(1L);
        category.setName("Work");

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setStatus(Status.IN_PROGRESS);
        task.setCategoryId(category);
        task.setUserId(user);
    }

    @Test
    void testAddNewTask() {
        when(userRepository.save(user)).thenReturn(user);

        User savedUser = taskService.addNewTask(user, task);

        assertTrue(savedUser.getTasks().contains(task));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testAddNewTask_nullUser_throwsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.addNewTask(null, task);
        });
        assertEquals("User is null", exception.getMessage());
    }

    @Test
    void testAddNewTask_nullTask_throwsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.addNewTask(user, null);
        });
        assertEquals("Task is null", exception.getMessage());
    }

    @Test
    void testFindTaskByUser() {
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findByUserId(user)).thenReturn(tasks);

        List<Task> result = taskService.findTaskByUser(user);

        assertEquals(1, result.size());
        assertEquals(task, result.get(0));
    }

    @Test
    void testFindTaskByUser_nullUser_throwsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.findTaskByUser(null);
        });
        assertEquals("User is null", exception.getMessage());
    }

    @Test
    void testFindByStatusAndCategory() {
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findByStatusAndCategoryId(Status.IN_PROGRESS, category)).thenReturn(tasks);

        List<Task> result = taskService.findByStatusAndCategory(Status.IN_PROGRESS, category);

        assertEquals(1, result.size());
        assertEquals(task, result.get(0));
    }

    @Test
    void testFindByStatusAndCategory_categoryNull() {
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findByStatus(Status.IN_PROGRESS)).thenReturn(tasks);

        List<Task> result = taskService.findByStatusAndCategory(Status.IN_PROGRESS, null);

        assertEquals(1, result.size());
        assertEquals(task, result.get(0));
    }

    @Test
    void testFindById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(task, result.get());
    }

    @Test
    void testRemoveTask() {
        taskService.removeTask(task);
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void testSaveTask() {
        taskService.save(task);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testFindTasksFiltered() {
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.findFiltered(user, "Test", 1L, Status.IN_PROGRESS, PageRequest.of(0, 10))).thenReturn(page);

        Page<Task> result = taskService.findTasksFiltered(user, "Test", 1L, Status.IN_PROGRESS, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(task, result.getContent().get(0));
    }
}
