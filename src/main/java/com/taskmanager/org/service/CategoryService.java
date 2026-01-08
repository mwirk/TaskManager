package com.taskmanager.org.service;

import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.CategoryRepository;
import com.taskmanager.org.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }
    @Transactional
    public Category addNewCategory(Category newCategory){
        return categoryRepository.save(newCategory);
    }
    @Transactional
    public void removeCategory(Category category){
        categoryRepository.delete(category);
    }
    @Transactional(readOnly = true)
    public List<Category> findById(Long id) {
        return categoryRepository.findCategoryById(id);
    }
    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }
}
