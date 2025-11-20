package com.taskmanager.org.dto;

import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Status;
import com.taskmanager.org.model.User;

import java.time.LocalDateTime;

public class TaskViewDTO {

    private Integer id;
    private String title;
    private String description;
    private Status status;
    private LocalDateTime dueDate;
    private Category categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private User userId;

    public TaskViewDTO() {}

    public TaskViewDTO(Integer id,
                       String title,
                       String description,
                       Status status,
                       LocalDateTime dueDate,
                       Category categoryId,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt,
                       User userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.categoryId = categoryId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public Category getCategoryId() {
        return categoryId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public User getUserId() {
        return userId;
    }
}
