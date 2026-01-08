package com.taskmanager.org.controller;

import com.taskmanager.org.exception.TaskNotFoundException;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.service.FileStorageService;
import com.taskmanager.org.service.TaskService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {
    private final FileStorageService fileStorageService;
    private final TaskService taskService;

    public FileUploadController(FileStorageService fileStorageService, TaskService taskService){
        this.fileStorageService = fileStorageService;
        this.taskService =taskService;
    }
    @PostMapping(value = "/photos/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile photo,
                                              @PathVariable Long id) throws IOException {

        List<Task> tasks = taskService.findTaskById(id);
        if (tasks.isEmpty()) {
            throw new TaskNotFoundException("Task not found");
        }
        Task task = tasks.get(0);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedUserEmail = authentication.getName();

        if (!task.getUserId().getEmail().equals(loggedUserEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to update this task");
        }

        String pathPhoto = fileStorageService.storeFile(photo);
        task.setPhoto(pathPhoto);
        taskService.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(pathPhoto);
    }
    @GetMapping("/photos/{id}")
    public ResponseEntity<Resource> getTaskPhoto(@PathVariable Long id) throws IOException {

        List<Task> tasks = taskService.findTaskById(id);
        if (tasks.isEmpty()) {
            throw new TaskNotFoundException("Task not found");
        }

        Task task = tasks.get(0);


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedUserEmail = authentication.getName();

        if (!task.getUserId().getEmail().equals(loggedUserEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String photoPath = task.getPhoto();
        if (photoPath == null || photoPath.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = fileStorageService.loadFile(photoPath);

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(filePath));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filePath.getFileName().toString() + "\"")
                .contentType(MediaType.parseMediaType(
                        Files.probeContentType(filePath) != null
                                ? Files.probeContentType(filePath)
                                : MediaType.APPLICATION_OCTET_STREAM_VALUE
                ))
                .contentLength(Files.size(filePath))
                .body(resource);
    }

}
