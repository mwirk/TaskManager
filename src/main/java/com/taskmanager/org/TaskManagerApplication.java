package com.taskmanager.org;

import com.taskmanager.org.service.CategoryService;
import com.taskmanager.org.service.TaskService;
import com.taskmanager.org.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class TaskManagerApplication{
    private final TaskService taskService;
    private final UserService userService;
    private final CategoryService categoryService;

    public TaskManagerApplication(TaskService taskService, UserService userService, CategoryService categoryService){
        this.taskService = taskService;
        this.userService = userService;
        this.categoryService = categoryService;
    }
    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);

    }
}

//curl -X POST http://localhost:8080/api/users        -H "Content-Type: application/json"        -d '{
//       "name": "Jan",
//       "email": "Jan@mail.com",
//         "password": "haslo"
//       }'