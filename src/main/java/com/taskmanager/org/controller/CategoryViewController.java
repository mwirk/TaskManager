package com.taskmanager.org.controller;

import com.taskmanager.org.dto.CategoryViewDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryViewController {
    private final TaskService taskService;
    private final CategoryService categoryService;
    private final UserService userService;
    public CategoryViewController(TaskService taskService, CategoryService categoryService, UserService userService){
        this.taskService = taskService;
        this.categoryService = categoryService;
        this.userService = userService;
    }


    @GetMapping("/add")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new CategoryViewDTO());
        return "categories/form";
    }

    @PostMapping("/add")
    public String createDepartment(
            @RequestParam("name") String name,
            @RequestParam("color") String color,
            RedirectAttributes redirectAttributes) {


        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Name is required");
            return "redirect:/color/add";
        }
        if (color == null || color.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Color is required");
            return "redirect:/color/add";
        }


        Category category = new Category(null, name, color);
        categoryService.addNewCategory(category);

        redirectAttributes.addFlashAttribute("success", "Category added successfully");

        return "redirect:/tasks/add";
    }
}

