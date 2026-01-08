package com.taskmanager.org;

import com.taskmanager.org.service.CategoryService;
import com.taskmanager.org.service.TaskService;
import com.taskmanager.org.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class TaskManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}


//curl -X POST http://localhost:8080/api/users        -H "Content-Type: application/json"        -d '{
//       "name": "Jan",
//       "email": "Jan@mail.com",
//         "password": "haslo"
//       }'

//curl -X POST http://localhost:8080/api/auth/login \
//        -H "Content-Type: application/json" \
//        -d '{
//        "email": "adam@mail.com",
//        "password": "Ha$lo1"
//        }'
//
////
//curl -X GET http://localhost:8080/api/v1/users   -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwic3ViIjoiYm9iQG1haWwuY29tIiwiaWF0IjoxNzY3ODA0NTczLCJleHAiOjE3Njc4MDgxNzN9.-owIFgLKolOZ2_QI1b9N8GHE29vZD1oGpasXHeBO4jE"   -H "Content-Type: application/json"
//
//curl -v -X POST "http://localhost:8080/api/v1/files/photos/ID TASKA" \
//        -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwic3ViIjoiYm9iQG1haWwuY29tIiwiaWF0IjoxNzY3ODA0NTczLCJleHAiOjE3Njc4MDgxNzN9.-owIFgLKolOZ2_QI1b9N8GHE29vZD1oGpasXHeBO4jE" \
//        -F "file=@/uploads/book.webp"
//
//curl -v -X GET "http://localhost:8080/api/v1/files/photos/1" \
//       -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwic3ViIjoiYWRhbUBtYWlsLmNvbSIsImlhdCI6MTc2NzgwNDgzMywiZXhwIjoxNzY3ODA4NDMzfQ.kooRKPk451_5vatjddmVM8evw5dak8Kncuel4l8t7WQ" \
//       --output downloaded_photo.jpg
//
//curl -v -X POST \
//        -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwic3ViIjoiYWRhbUBtYWlsLmNvbSIsImlhdCI6MTc2NzgwNDgzMywiZXhwIjoxNzY3ODA4NDMzfQ.kooRKPk451_5vatjddmVM8evw5dak8Kncuel4l8t7WQ" \
//        -F "file=C:\Users\mwirk\OneDrive\Pulpit\AP_projekt\TaskManageruploads\book.webp" \
//http://localhost:8080/api/v1/files/photos/1



//curl -v \
//        -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwic3ViIjoiYWRhbUBtYWlsLmNvbSIsImlhdCI6MTc2NzgwNDgzMywiZXhwIjoxNzY3ODA4NDMzfQ.kooRKPk451_5vatjddmVM8evw5dak8Kncuel4l8t7WQ" \
//        -F "file=@/c/Users/mwirk/OneDrive/Pulpit/AP_projekt/TaskManager/uploads/book.webp" \
//http://localhost:8080/api/v1/files/photos/1
