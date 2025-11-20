package com.taskmanager.org.controller;


import com.taskmanager.org.dto.UserDTO;
import com.taskmanager.org.exception.SameEmailException;
import com.taskmanager.org.exception.UserNotFoundException;
import com.taskmanager.org.model.User;
import com.taskmanager.org.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;

    }
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @RequestParam(required = false) String email) {
        List<User> users;
        if (email != null)
            users = userService.findByEmail(email);
        else
            users = userService.findAllUsers();

        List<UserDTO> userDTOs = users.stream()
                .map(user -> new UserDTO(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @PostMapping
    public ResponseEntity<UserDTO> addNewUser(@RequestBody UserDTO requestUserDTO){
        User user = new User(
                requestUserDTO.getId(),
                requestUserDTO.getName(),
                requestUserDTO.getEmail(),
                requestUserDTO.getPassword()
        );

        userService.addNewUser(user);

        UserDTO response = new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @DeleteMapping("/{email}")
    public ResponseEntity<UserDTO> removeUser(@PathVariable String email) {
        List<User> users = userService.findByEmail(email);
        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        userService.removeUser(users.getFirst());
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{email}")
    public ResponseEntity<UserDTO> updateUserCredentials(
            @RequestBody UserDTO requestUserDTO,
            @PathVariable String email) {


        List<User> users = userService.findByEmail(email);
        if (users.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        User user = users.getFirst();


        if (!requestUserDTO.getEmail().equals(email)) {
            List<User> potentialUsers = userService.findByEmail(requestUserDTO.getEmail());
            if (!potentialUsers.isEmpty()) {
                throw new SameEmailException("Account with such email already exists");
            }
            user.setEmail(requestUserDTO.getEmail());
        }
        user.setName(requestUserDTO.getName());
        user.setPassword(requestUserDTO.getPassword());

        UserDTO response = new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
        return ResponseEntity.ok(response);
    }



}
