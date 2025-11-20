package com.taskmanager.org.repository;

import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Status;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByStatus(Status status);
    List<Task> findByUserId(User user);
    List<Task> findTaskById(Integer id);
    List<Task> findByStatusAndCategoryId(Status status, Category categoryId);
    List<Task> findByCategoryId(Category categoryId);
    List<Task> findAll();
}
