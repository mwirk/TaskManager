package com.taskmanager.org.controller;

import com.taskmanager.org.dto.UserDTO;
import com.taskmanager.org.model.User;
import com.taskmanager.org.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService){
        this.userService = userService;

    }
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }
    @PostMapping("/register")
    public String register(UserDTO requestUserDTO){
        User user = new User(
                requestUserDTO.getId(),
                requestUserDTO.getName(),
                requestUserDTO.getEmail(),
                requestUserDTO.getPassword()
        );

        userService.addNewUser(user);

        return "login";
    }

}