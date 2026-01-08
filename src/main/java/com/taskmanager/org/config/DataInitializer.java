package com.taskmanager.org.config;

import com.taskmanager.org.model.Role;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.RoleRepository;
import com.taskmanager.org.repository.UserRepository;
import com.taskmanager.org.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {


        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));


        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));


        if (!userRepository.existsByEmail("admin@admin.com")) {
            User user = new User(
                    null,
                    "Admin",
                    "admin@admin.com",
                    "admin123"
            );

            user.setRoles(Set.of(adminRole));
            userService.addNewUser(user);

            System.out.println("Default admin created: admin@admin.com / admin123");
        }
    }
}

