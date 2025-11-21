package com.taskmanager.org.controller;

import com.taskmanager.org.dto.TaskViewDTO;
import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Status;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import com.taskmanager.org.service.CategoryService;
import com.taskmanager.org.service.TaskService;
import com.taskmanager.org.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskViewController {
    private final TaskService taskService;
    private final CategoryService categoryService;
    private final UserService userService;
    public TaskViewController(TaskService taskService, CategoryService categoryService, UserService userService){
        this.taskService = taskService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping
    public String tasksPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByEmail(username).getFirst();
        List<Task> tasks = taskService.findTaskByUser(user);

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
    @GetMapping("/add")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new TaskViewDTO());
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories", categories);
        return "tasks/form";
    }

    @PostMapping("/add")
    public String createDepartment(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") Category category,
            @RequestParam("dueDate") LocalDateTime dueDate,
            RedirectAttributes redirectAttributes) {


        if (title == null || title.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Title is required");
            return "redirect:/tasks/add";
        }
        if (description == null || description.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Description is required");
            return "redirect:/tasks/add";
        }
        if (category == null) {
            redirectAttributes.addFlashAttribute("error", "Category is required");
            return "redirect:/tasks/add";
        }
        if (dueDate == null) {
            redirectAttributes.addFlashAttribute("error", "Due date is required");
            return "redirect:/tasks/add";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByEmail(username).getFirst();
        Task task = new Task(null, title, description, Status.TO_DO, dueDate, category, LocalDateTime.now(), LocalDateTime.now(),user);
        taskService.addNewTask(user, task);

        redirectAttributes.addFlashAttribute("success", "Task added successfully");

        return "redirect:/tasks";
    }
    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable int id, RedirectAttributes redirectAttributes) {
        taskService.removeTask(taskService.findTaskById(id).getFirst());
        redirectAttributes.addFlashAttribute("success", "Task deleted");
        return "redirect:/tasks";
    }
}

