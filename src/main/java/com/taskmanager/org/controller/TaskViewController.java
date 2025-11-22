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
    public String tasksPage(@RequestParam(required = false) String title,
                            @RequestParam(required = false) Category category,
                            @RequestParam(required = false) Status status,
                            Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByEmail(username).getFirst();

        List<Task> tasks = taskService.findTaskByUser(user);

        if (title != null && !title.isBlank()) {
            tasks = tasks.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .toList();
        }

        if (category != null) {
            tasks = tasks.stream()
                    .filter(t -> t.getCategoryId().getId() == category.getId())
                    .toList();
        }
        if (status != null){
            tasks = tasks.stream()
                    .filter(t -> t.getStatus() == status)
                    .toList();
        }

        List<TaskViewDTO> dtos = tasks.stream()
                .map(task -> new TaskViewDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getDueDate(),
                        task.getCategoryId(),
                        task.getCreatedAt(),
                        task.getUpdatedAt(),
                        task.getUserId()
                ))
                .toList();

        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("tasks", dtos);
        model.addAttribute("statuses", Status.values());

        return "tasks/list";
    }
    @GetMapping("/add")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new TaskViewDTO());
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("statuses", Status.values());
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
    @GetMapping("/edit/{id}")
    public String getEditForm(@PathVariable int id,Model model){
        Task task = taskService.findTaskById(id).getFirst();
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("task", task);
        model.addAttribute("statuses", Status.values());
        return "tasks/editForm";
    }
    @PostMapping("/edit/{id}")
    public String changeTask(@PathVariable int id,
                             @RequestParam("title") String title,
                             @RequestParam("description") String description,
                             @RequestParam("category") Category category,
                             @RequestParam("status") Status status,
                             @RequestParam("dueDate") LocalDateTime dueDate,
                             RedirectAttributes redirectAttributes) {
        if (title == null || title.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Title is required");
            return "redirect:/tasks/edit/" + id;
        }
        if (description == null || description.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Description is required");
            return "redirect:/tasks/edit/" + id;
        }
        if (category == null) {
            redirectAttributes.addFlashAttribute("error", "Category is required");
            return "redirect:/tasks/edit/" + id;
        }
        if (dueDate == null) {
            redirectAttributes.addFlashAttribute("error", "Due date is required");
            return "redirect:/tasks/edit/" + id;
        }
        Task task = taskService.findTaskById(id).getFirst();
        task.setTitle(title);
        task.setDescription(description);
        task.setCategoryId(category);
        task.setDueDate(dueDate);
        task.setUpdatedAt(LocalDateTime.now());
        task.setStatus(status);
        taskService.save(task);
        redirectAttributes.addFlashAttribute("success", "Task edited");
        return "redirect:/tasks";
    }
}

