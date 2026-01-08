package com.taskmanager.org.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileStorageService {

    private final Path uploadPath;
    private final List<String> allowedExtensions;

    @Value("${app.upload.max-photo-size}")
    private long maxSizeMB;

    public long getMaxSizeMB() {
        return maxSizeMB;
    }

    public void setMaxSizeMB(long maxSizeMB) {
        this.maxSizeMB = maxSizeMB;
    }

    public FileStorageService(
            @Value("${app.upload.directory}") String uploadDir,
            @Value("${app.upload.allowed-extensions}") String extensions) {


        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.allowedExtensions = Arrays.stream(extensions.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());


        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot create upload directory", ex);
        }
    }

    public String storeFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("File must have a name");
        }


        long maxBytes = maxSizeMB * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new MaxUploadSizeExceededException(maxBytes);
        }


        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException(
                    "Invalid file extension. Allowed: " + allowedExtensions);
        }


        String filename = generateUniqueFilename(extension);
        Path targetLocation = this.uploadPath.resolve(filename);
        System.out.println("File saved to: " + targetLocation.toAbsolutePath());

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException ex) {
            throw new RuntimeException("Error saving file", ex);
        }
    }

    public Path loadFile(String filename) {
        return uploadPath.resolve(filename).normalize();
    }

    public void deleteFile(String filename) {
        try {
            Path filePath = uploadPath.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Error deleting file", ex);
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? "" : filename.substring(lastDot + 1);
    }

    private String generateUniqueFilename(String extension) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        return "file_" + timestamp + "." + extension;
    }
}
