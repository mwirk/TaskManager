package com.taskmanager.org.restControllerTest;

import com.taskmanager.org.controller.FileUploadController;
import com.taskmanager.org.exception.GlobalExceptionHandler;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import com.taskmanager.org.service.FileStorageService;
import com.taskmanager.org.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FileUploadControllerTest {

    private FileUploadController controller;
    private FileStorageService fileStorageService;
    private TaskService taskService;
    private MockMvc mockMvc;
    private Path tempUploadDir;

    @BeforeEach
    void setUp() throws Exception {
        tempUploadDir = Files.createTempDirectory("upload_test");

        fileStorageService = Mockito.mock(FileStorageService.class);
        taskService = Mockito.mock(TaskService.class);

        controller = new FileUploadController(fileStorageService, taskService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Mock SecurityContextHolder globally
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getName()).thenReturn("test@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testUploadPhoto_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "Hello".getBytes()
        );

        Task mockTask = new Task();
        User user = new User();
        user.setEmail("test@example.com");
        mockTask.setUserId(user);
        mockTask.setPhoto(null);

        Mockito.when(fileStorageService.storeFile(any())).thenReturn("saved_test.jpg");
        Mockito.when(taskService.findTaskById(anyLong())).thenReturn(List.of(mockTask));

        mockMvc.perform(multipart("/api/v1/files/photos/{id}", 1L)
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(content().string("saved_test.jpg"));

        assert mockTask.getPhoto().equals("saved_test.jpg");
    }

    @Test
    void testUploadPhoto_TaskNotFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "Hello".getBytes()
        );

        Mockito.when(taskService.findTaskById(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(multipart("/api/v1/files/photos/{id}", 1L)
                        .file(file))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void testGetPhoto_Success() throws Exception {
        Task task = new Task();
        User user = new User();
        user.setEmail("test@example.com");
        task.setUserId(user);
        task.setPhoto("photo.jpg");

        Path filePath = tempUploadDir.resolve("photo.jpg");
        Files.write(filePath, "filedata".getBytes());

        Mockito.when(taskService.findTaskById(anyLong())).thenReturn(List.of(task));
        Mockito.when(fileStorageService.loadFile(any())).thenReturn(filePath);

        mockMvc.perform(get("/api/v1/files/photos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"photo.jpg\""));
    }

    @Test
    void testGetPhoto_TaskNotFound() throws Exception {
        Mockito.when(taskService.findTaskById(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/files/photos/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void testGetPhoto_NoPhotoSet() throws Exception {
        Task task = new Task();
        User user = new User();
        user.setEmail("test@example.com");
        task.setUserId(user);
        task.setPhoto(null);

        Mockito.when(taskService.findTaskById(anyLong())).thenReturn(List.of(task));

        mockMvc.perform(get("/api/v1/files/photos/{id}", 1L))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(username = "otheruser@example.com")
    void testGetPhoto_NotOwner_ShouldReturnForbidden() throws Exception {

        Task task = new Task();
        User owner = new User();
        owner.setEmail("owner@example.com");
        task.setUserId(owner);
        task.setPhoto("photo.jpg");

        Mockito.when(taskService.findTaskById(anyLong())).thenReturn(List.of(task));

        mockMvc.perform(get("/api/v1/files/photos/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetPhoto_Owner_ShouldReturnFile() throws Exception {
        Task task = new Task();
        User owner = new User();
        owner.setEmail("test@example.com");
        task.setUserId(owner);
        task.setPhoto("photo.jpg");


        Path filePath = tempUploadDir.resolve("photo.jpg");
        Files.write(filePath, "filedata".getBytes());

        Mockito.when(taskService.findTaskById(anyLong())).thenReturn(List.of(task));
        Mockito.when(fileStorageService.loadFile(any())).thenReturn(filePath);

        mockMvc.perform(get("/api/v1/files/photos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"photo.jpg\""))
                .andExpect(content().bytes("filedata".getBytes()));
    }
    @Test
    void testGetPhoto_FileNotFoundOnDisk() throws Exception {
        Task task = new Task();
        User owner = new User();
        owner.setEmail("test@example.com");
        task.setUserId(owner);
        task.setPhoto("missing.jpg"); // plik nie istnieje

        Mockito.when(taskService.findTaskById(anyLong())).thenReturn(List.of(task));
        Mockito.when(fileStorageService.loadFile("missing.jpg"))
                .thenReturn(tempUploadDir.resolve("missing.jpg")); // wskazuje na nieistniejący plik

        mockMvc.perform(get("/api/v1/files/photos/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPhoto_PhotoPathNullOrBlank() throws Exception {
        Task task = new Task();
        User owner = new User();
        owner.setEmail("test@example.com");
        task.setUserId(owner);
        task.setPhoto(""); // pusty path

        Mockito.when(taskService.findTaskById(anyLong())).thenReturn(List.of(task));

        mockMvc.perform(get("/api/v1/files/photos/{id}", 1L))
                .andExpect(status().isNotFound());

        task.setPhoto(null); // null path

        mockMvc.perform(get("/api/v1/files/photos/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUploadPhoto_NotOwner_ShouldReturnForbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "Hello".getBytes()
        );

        Task task = new Task();
        User owner = new User();
        owner.setEmail("otheruser@example.com");
        task.setUserId(owner);
        task.setPhoto(null);

        Mockito.when(taskService.findTaskById(anyLong())).thenReturn(List.of(task));

        mockMvc.perform(multipart("/api/v1/files/photos/{id}", 1L)
                        .file(file))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You are not allowed to update this task"));
    }

}
