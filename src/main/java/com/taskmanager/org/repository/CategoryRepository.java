package com.taskmanager.org.repository;

import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findCategoryById(Long id);


}
