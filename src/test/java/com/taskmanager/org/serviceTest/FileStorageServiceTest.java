package com.taskmanager.org.serviceTest;

import com.taskmanager.org.exception.GlobalExceptionHandler;
import com.taskmanager.org.service.FileStorageService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private Path tempUploadDir;
    private MockMvc mockMvc;
    @BeforeEach
    void setUp() throws IOException {

        tempUploadDir = Files.createTempDirectory("upload_test");

        fileStorageService = new FileStorageService(
                tempUploadDir.toString(),
                "jpg,png"
        );




        fileStorageService.setMaxSizeMB(1);
    }

    @AfterEach
    void tearDown() throws IOException {

        Files.walk(tempUploadDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try { Files.deleteIfExists(path); }
                    catch (IOException ignored) {}
                });
    }

    private MultipartFile mockFile(String name, byte[] content) throws IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);

        Mockito.when(file.getOriginalFilename()).thenReturn(name);
        Mockito.when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        Mockito.when(file.getSize()).thenReturn((long) content.length);
        Mockito.when(file.isEmpty()).thenReturn(content.length == 0);

        return file;
    }

    @Test
    void testStoreFile_SuccessfulSave() throws IOException {
        MultipartFile file = mockFile("photo.jpg", "hello".getBytes());

        String savedName = fileStorageService.storeFile(file);

        assertNotNull(savedName);
        assertTrue(savedName.endsWith(".jpg"));
        assertTrue(Files.exists(tempUploadDir.resolve(savedName)));
    }

    @Test
    void testStoreFile_EmptyFile_ThrowsException() throws IOException {
        MultipartFile file = mockFile("photo.jpg", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    void testStoreFile_BlankName_ThrowsException() throws IOException {
        MultipartFile file = mockFile("   ", "data".getBytes());

        Mockito.when(file.getOriginalFilename()).thenReturn("  ");

        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    void testStoreFile_InvalidExtension_ThrowsException() throws IOException {
        MultipartFile file = mockFile("virus.exe", "boom".getBytes());

        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    void testStoreFile_TooLarge_ThrowsMaxUploadSizeException() throws IOException {

        MultipartFile file = mockFile("photo.jpg", new byte[2 * 1024 * 1024]);

        assertThrows(MaxUploadSizeExceededException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    void testLoadFile_ReturnPath() {
        Path loaded = fileStorageService.loadFile("abc.png");

        assertEquals(tempUploadDir.resolve("abc.png").normalize(), loaded);
    }

    @Test
    void testDeleteFile_RemovesExistingFile() throws IOException {
        MultipartFile file = mockFile("image.jpg", "ok".getBytes());
        String savedName = fileStorageService.storeFile(file);

        Path savedPath = tempUploadDir.resolve(savedName);
        assertTrue(Files.exists(savedPath));

        fileStorageService.deleteFile(savedName);

        assertFalse(Files.exists(savedPath));
    }

    @Test
    void testDeleteFile_NoExceptionIfNotExists() {
        assertDoesNotThrow(() ->
                fileStorageService.deleteFile("not_existing.jpg")
        );
    }
}
