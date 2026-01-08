package com.taskmanager.org.controller;

import com.taskmanager.org.dto.UserDTO;
import com.taskmanager.org.model.Role;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.RoleRepository;
import com.taskmanager.org.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import java.util.Set;

@Controller
public class AuthController {
    private final UserService userService;
    private final RoleRepository roleRepository;
    public AuthController(UserService userService, RoleRepository roleRepository){
        this.userService = userService;
        this.roleRepository = roleRepository;

    }
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("success", "Logout successfully");
        }
        return "login";
    }
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") UserDTO userDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            User user = new User(
                    userDTO.getId(),
                    userDTO.getName(),
                    userDTO.getEmail(),
                    userDTO.getPassword()
            );

            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
            user.setRoles(Set.of(userRole));

            userService.addNewUser(user);

            model.addAttribute("success", "Account created successfully. You can login now.");
            return "login";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }




}