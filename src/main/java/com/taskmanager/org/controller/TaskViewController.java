package com.taskmanager.org.controller;

import com.taskmanager.org.dto.TaskViewDTO;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TaskViewController {
    private final TaskService taskService;
    public TaskViewController(TaskService taskService){
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public String tasksPage(Model model) {
        List<Task> tasks = taskService.findAllTasks();

        List<TaskViewDTO> taskViewDTOS = new ArrayList<>();
        for (Task task : tasks) {
            TaskViewDTO dto = new TaskViewDTO(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getDueDate(),
                    task.getCategoryId(),
                    task.getCreatedAt(),
                    task.getUpdatedAt(),
                    task.getUserId()

            );
            taskViewDTOS.add(dto);
        }

        model.addAttribute("tasks", taskViewDTOS);
        return "tasks/list";
    }
}

