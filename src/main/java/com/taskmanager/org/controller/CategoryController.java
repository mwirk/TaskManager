package com.taskmanager.org.controller;

import com.taskmanager.org.dto.CategoryDTO;
import com.taskmanager.org.exception.CategoryNotFoundException;
import com.taskmanager.org.model.Category;
import com.taskmanager.org.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @Operation(summary = "Get categories", description = "Retrieve all categories or a specific category by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping({"", "/{id}"})
    public ResponseEntity<List<CategoryDTO>> getAllCategories(
            @Parameter(description = "Optional category ID to fetch a specific category") @PathVariable(required = false) Long id){

        List<Category> categories;
        if (id != null) {
            categories = categoryService.findById(id);
            if (categories.isEmpty()){
                throw new CategoryNotFoundException("Category not found");
            }
        } else {
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

    @Operation(summary = "Create a new category", description = "Adds a new category with a name and color.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category created successfully")
    })
    @PostMapping
    public ResponseEntity<CategoryDTO> addCategoryTask(
            @Parameter(description = "Category details") @RequestBody CategoryDTO requestCategoryDTO){

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

    @Operation(summary = "Delete a category", description = "Deletes a category by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeCategory(
            @Parameter(description = "ID of the category to delete") @PathVariable Long id) {

        List<Category> categories = categoryService.findById(id);
        if (categories.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        categoryService.removeCategory(categories.getFirst());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a category", description = "Updates the name and color of an existing category by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategoryCredentials(
            @Parameter(description = "Updated category details") @RequestBody CategoryDTO requestCategoryDTO,
            @Parameter(description = "ID of the category to update") @PathVariable Long id) {

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
