package com.taskmanager.org.controller;

import com.taskmanager.org.dto.TaskDTO;
import com.taskmanager.org.exception.InvalidUserIdForTaskException;
import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Status;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import com.taskmanager.org.service.TaskService;
import com.taskmanager.org.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService){
        this.taskService = taskService;
        this.userService = userService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Category categoryId) {

        List<Task> tasks;

        if (status != null && categoryId != null) {
            tasks = taskService.findByStatusAndCategory(status, categoryId);
        } else if (status != null) {
            tasks = taskService.findByStatus(status);
        } else if (categoryId != null) {
            tasks = taskService.findByCategory(categoryId);
        } else {
            tasks = taskService.findAllTasks();
        }
        
        List<TaskDTO> taskDTOs = tasks.stream()
                .map(task -> new TaskDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getDueDate(),
                        task.getCategoryId(),
                        task.getCreatedAt(),
                        task.getUpdatedAt(),
                        task.getUserId() != null ? task.getUserId().getId() : null
                ))
                .toList();

        return ResponseEntity.ok(taskDTOs);
    }
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getSpecificTask(
            @RequestParam(required = false) Integer id) {

        List<Task> tasks = taskService.findTaskById(id);

        if (tasks.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Task task = tasks.getFirst();
        TaskDTO response = new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getCategoryId(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getUserId().getId()
        );
        return ResponseEntity.ok(response);
    }
    @PostMapping
    public ResponseEntity<TaskDTO> addNewTask(@RequestBody TaskDTO requestTaskDTO){

        if (requestTaskDTO.getUserId() == null) {
            throw new InvalidUserIdForTaskException("UserId is null");
        }


        User user = userService.findById(requestTaskDTO.getUserId())
                .orElseThrow(() -> new InvalidUserIdForTaskException("User not found"));


        Task task = new Task(
                requestTaskDTO.getId(),
                requestTaskDTO.getTitle(),
                requestTaskDTO.getDescription(),
                requestTaskDTO.getStatus(),
                requestTaskDTO.getDueDate(),
                requestTaskDTO.getCategoryId(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                user
        );

        taskService.addNewTask(user, task);

        TaskDTO response = new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getCategoryId(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getUserId().getId()
        );

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeUser(@PathVariable Integer id) {
        List<Task> tasks = taskService.findTaskById(id);

        if (tasks.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        taskService.removeTask(tasks.getFirst());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTaskCredentials(
            @RequestBody TaskDTO requestTaskDTO,
            @PathVariable Integer id) {


        List<Task> tasks = taskService.findTaskById(id);
        if (tasks.isEmpty()) {
            throw new EntityNotFoundException("Task not found");
        }
        Task task = tasks.getFirst();
        task.setTitle(requestTaskDTO.getTitle());
        task.setDescription(requestTaskDTO.getDescription());
        task.setStatus(requestTaskDTO.getStatus());
        task.setDueDate(requestTaskDTO.getDueDate());
        task.setUpdatedAt(LocalDateTime.now());


        TaskDTO response = new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getCategoryId(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getUserId().getId()
        );
        return ResponseEntity.ok(response);
    }
}
