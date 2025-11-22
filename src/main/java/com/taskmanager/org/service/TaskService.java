package com.taskmanager.org.service;

import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Status;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.TaskRepository;
import com.taskmanager.org.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public User addNewTask(User user, Task task) {
        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }
        if (task == null) {
            throw new IllegalArgumentException("Task is null");
        }
        task.setUserId(user);
        user.getTasks().add(task);

        return userRepository.save(user);
    }
    public List<Task> findTaskByUser(User user){
        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }
        List<Task> tasks = taskRepository.findByUserId(user);
        return tasks;

    }

    public List<Task> findByStatusAndCategory(Status status, Category categoryId) {
        if (categoryId != null) {
            return taskRepository.findByStatusAndCategoryId(status, categoryId);
        } else {
            return taskRepository.findByStatus(status);
        }
    }

    public List<Task> findByStatus(Status status) {
        return taskRepository.findByStatus(status);
    }

    public List<Task> findByCategory(Category categoryId) {
        return taskRepository.findByCategoryId(categoryId);
    }

    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> findById(Integer id) {
        return taskRepository.findById(id);
    }

    public void removeTask(Task taskToBeRemoved) {
        taskRepository.delete(taskToBeRemoved);
    }

    public List<Task> findTaskById(Integer id) {
        return taskRepository.findTaskById(id);
    }

    public void save(Task task) {
        taskRepository.save(task);
    }
}
