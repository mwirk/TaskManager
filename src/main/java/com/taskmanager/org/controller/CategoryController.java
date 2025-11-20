package com.taskmanager.org.controller;

import com.taskmanager.org.dto.CategoryDTO;
import com.taskmanager.org.dto.TaskDTO;
import com.taskmanager.org.dto.UserDTO;
import com.taskmanager.org.exception.InvalidUserIdForTaskException;
import com.taskmanager.org.exception.UserNotFoundException;
import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import com.taskmanager.org.service.CategoryService;
import com.taskmanager.org.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories(@RequestParam(required = false) Integer id){
        List<Category> categories;
        if (id != null)
            categories = categoryService.findById(id);

        else {
            categories = categoryService.findAllCategories();
        }
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> new CategoryDTO(
                        category.getId(),
                        category.getName(),
                        category.getColor()
                ))
                .toList();

        return ResponseEntity.ok(categoryDTOs);
    }
    @PostMapping
    public ResponseEntity<CategoryDTO> addCategoryTask(@RequestBody CategoryDTO requestCategoryDTO){


        Category category = new Category(
                requestCategoryDTO.getId(),
                requestCategoryDTO.getName(),
                requestCategoryDTO.getColor()
        );

        categoryService.addNewCategory(category);

        CategoryDTO response = new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getColor()

        );

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeCategory(@PathVariable Integer id) {
        List<Category> categories = categoryService.findById(id);

        if (categories.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        categoryService.removeCategory(categories.getFirst());
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategoryCredentials(
            @RequestBody CategoryDTO requestCategoryDTO,
            @PathVariable Integer id) {


        List<Category> categories = categoryService.findById(id);
        if (categories.isEmpty()) {
            throw new EntityNotFoundException("Category not found");
        }
        Category category = categories.getFirst();
        category.setName(requestCategoryDTO.getName());
        category.setColor(requestCategoryDTO.getColor());


        CategoryDTO response = new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getColor()

        );
        return ResponseEntity.ok(response);
    }

}
