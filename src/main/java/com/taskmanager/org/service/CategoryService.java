package com.taskmanager.org.service;

import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.CategoryRepository;
import com.taskmanager.org.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }
    public Category addNewCategory(Category newCategory){
        return categoryRepository.save(newCategory);
    }
    public void removeCategory(Category category){
        categoryRepository.delete(category);
    }

    public List<Category> findById(Integer id) {
        return categoryRepository.findCategoryById(id);
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }
}
