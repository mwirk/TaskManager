package com.taskmanager.org.dto;

import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Status;

import java.time.LocalDateTime;

public class TaskDTO {

    private Integer id;
    private String title;
    private String description;
    private Status status;
    private LocalDateTime dueDate;
    private Category categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer userId;

    public TaskDTO() {}

    public TaskDTO(Integer id, String title, String description, Status status,
                   LocalDateTime dueDate, Category categoryId,
                   LocalDateTime createdAt, LocalDateTime updatedAt,
                   Integer userId) {
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


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Category getCategoryId() { return categoryId; }
    public void setCategoryId(Category categoryId) { this.categoryId = categoryId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}
