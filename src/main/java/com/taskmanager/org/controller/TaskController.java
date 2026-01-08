package com.taskmanager.org.controller;

import com.taskmanager.org.dto.TaskDTO;
import com.taskmanager.org.exception.InvalidUserIdForTaskException;
import com.taskmanager.org.exception.TaskNotFoundException;
import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Status;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import com.taskmanager.org.service.TaskService;
import com.taskmanager.org.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

    @Operation(summary = "Get all tasks", description = "Returns a list of all tasks. Optional filters for status and category can be provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks(
            @Parameter(description = "Filter by task status") @RequestParam(required = false) Status status,
            @Parameter(description = "Filter by task category") @RequestParam(required = false) Category categoryId) {

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

    @Operation(summary = "Get a specific task", description = "Retrieve a task by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getSpecificTask(
            @Parameter(description = "ID of the task to retrieve") @PathVariable Long id) {

        List<Task> tasks = taskService.findTaskById(id);
        if (tasks.isEmpty()) {
            throw new TaskNotFoundException("Task not found");
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
                task.getUserId() != null ? task.getUserId().getId() : null
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a new task", description = "Adds a new task to the system. A valid userId must be provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID provided")
    })
    @PostMapping
    public ResponseEntity<TaskDTO> addNewTask(
            @Parameter(description = "Task details") @RequestBody TaskDTO requestTaskDTO) {

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

    @Operation(summary = "Delete a task", description = "Deletes a task by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeUser(
            @Parameter(description = "ID of the task to delete") @PathVariable Long id) {

        List<Task> tasks = taskService.findTaskById(id);
        if (tasks.isEmpty()) {
            throw new TaskNotFoundException("Task not found");
        }

        taskService.removeTask(tasks.getFirst());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a task", description = "Updates an existing task's details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTaskCredentials(
            @Parameter(description = "Updated task details") @RequestBody TaskDTO requestTaskDTO,
            @Parameter(description = "ID of the task to update") @PathVariable Long id) {

        List<Task> tasks = taskService.findTaskById(id);
        if (tasks.isEmpty()) {
            throw new TaskNotFoundException("Task not found");
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

    @Operation(summary = "Export tasks as CSV", description = "Exports tasks for a given user as a CSV file. If userId is not provided, exports all tasks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV exported successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error during CSV export")
    })
    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportCsvFile(
            @Parameter(description = "Optional user ID to filter tasks") @RequestParam(value = "userId", required = false) Long userId) {
        try {
            List<Task> tasks;
            if (userId != null) {
                User user = userService.findById(userId).stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("User not found: "));
                tasks = taskService.findTaskByUser(user);
            } else {
                tasks = taskService.findAllTasks();
            }

            StringBuilder csvBuilder = new StringBuilder();
            csvBuilder.append("Id,Title,Description,Status,DueDate,Category,CreatedAt,UpdatedAt\n");
            for (Task task : tasks) {
                csvBuilder
                        .append(task.getId()).append(",")
                        .append(task.getTitle()).append(",")
                        .append(task.getDescription()).append(",")
                        .append(task.getStatus()).append(",")
                        .append(task.getDueDate()).append(",")
                        .append(task.getCategoryId().getName()).append(",")
                        .append(task.getCreatedAt()).append(",")
                        .append(task.getUpdatedAt()).append("\n");
            }

            byte[] csvBytes = csvBuilder.toString().getBytes("UTF-8");
            ByteArrayResource resource = new ByteArrayResource(csvBytes);

            HttpHeaders headers = new HttpHeaders();
            String fileName = (userId != null) ? "tasks_" + userId + ".csv" : "tasks.csv";
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(csvBytes.length)
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
