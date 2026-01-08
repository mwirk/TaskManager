package com.taskmanager.org.controller;

import com.taskmanager.org.dto.TaskViewDTO;
import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Status;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import com.taskmanager.org.service.CategoryService;
import com.taskmanager.org.service.TaskService;
import com.taskmanager.org.service.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
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
    public float calculateDonePercentage(LocalDateTime createdAt, LocalDateTime dueDate) {


        if (createdAt == null || dueDate == null) {
            return 0f;
        }

        long totalMinutes = Duration.between(createdAt, dueDate).toMinutes();

        if (totalMinutes <= 0) {
            return 0f;
        }

        long passedMinutes = Duration.between(createdAt, LocalDateTime.now()).toMinutes();

        float percentage = (passedMinutes / (float) totalMinutes) * 100f;

        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;

        return percentage;
    }
    @GetMapping
    public String tasksPage(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String title,
                            @RequestParam(required = false) Long category,
                            @RequestParam(required = false) Status status,
                            @RequestParam(required = false) String sort,
                            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName()).getFirst();

        Sort sortObj = Sort.unsorted();
        if ("Status".equals(sort)) {
            sortObj = Sort.by("status").ascending();
        } else if ("Due date".equals(sort)) {
            sortObj = Sort.by("dueDate").ascending();
        }

        Pageable pageable = PageRequest.of(page, 10, sortObj);


        Page<Task> tasksPage = taskService.findTasksFiltered(user, title, category, status, pageable);

        List<TaskViewDTO> dtos = tasksPage.getContent().stream()
                .map(task -> new TaskViewDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getDueDate(),
                        task.getCategoryId(),
                        task.getCreatedAt(),
                        task.getUpdatedAt(),
                        task.getUserId(),
                        calculateDonePercentage(task.getCreatedAt(), task.getDueDate())
                ))
                .toList();

        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("tasks", dtos);
        model.addAttribute("statuses", Status.values());
        model.addAttribute("to_doCounter",
                taskService.findTaskByUser(user).stream().filter(t -> t.getStatus() == Status.TO_DO).count());

        model.addAttribute("in_progressCounter",
                taskService.findTaskByUser(user).stream().filter(t -> t.getStatus() == Status.IN_PROGRESS).count());

        model.addAttribute("doneCounter",
                taskService.findTaskByUser(user).stream().filter(t -> t.getStatus() == Status.DONE).count());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tasksPage.getTotalPages());

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
    public String createTask(
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
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        taskService.removeTask(taskService.findTaskById(id).getFirst());
        redirectAttributes.addFlashAttribute("success", "Task deleted");
        return "redirect:/tasks";
    }
    @GetMapping("/edit/{id}")
    public String getEditForm(@PathVariable Long id,Model model){
        Task task = taskService.findTaskById(id).getFirst();
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("task", task);
        model.addAttribute("statuses", Status.values());
        return "tasks/editForm";
    }
    @PostMapping("/edit/{id}")
    public String changeTask(@PathVariable Long id,
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








