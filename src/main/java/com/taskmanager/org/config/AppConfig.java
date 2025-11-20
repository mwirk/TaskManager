package com.taskmanager.org.config;

import com.taskmanager.org.repository.CategoryRepository;
import com.taskmanager.org.repository.TaskRepository;
import com.taskmanager.org.repository.UserRepository;
import com.taskmanager.org.service.CategoryService;
import com.taskmanager.org.service.TaskService;
import com.taskmanager.org.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public TaskService taskService(TaskRepository taskRepository, UserRepository userRepository) {
        return new TaskService(taskRepository, userRepository);
    }
    @Bean
    public CategoryService categoryService(CategoryRepository categoryRepository){
        return new CategoryService(categoryRepository);
    }
    @Bean
    public UserService userService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        return new UserService(userRepository, passwordEncoder);
    }


}

