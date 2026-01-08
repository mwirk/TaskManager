package com.taskmanager.org.controller;

import com.taskmanager.org.dto.UserDTO;
import com.taskmanager.org.exception.SameEmailException;
import com.taskmanager.org.model.Role;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.RoleRepository;
import com.taskmanager.org.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @Operation(summary = "Get all users or filter by email", description = "Returns a list of users. Admin access required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam(required = false) String email) {
        List<User> users = (email != null) ? userService.findByEmail(email) : userService.findAllUsers();

        List<UserDTO> userDTOs = users.stream()
                .map(user -> new UserDTO(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @Operation(summary = "Add a new user", description = "Creates a new user with ROLE_USER by default")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<UserDTO> addNewUser(@RequestBody UserDTO requestUserDTO) {
        User user = new User(
                requestUserDTO.getId(),
                requestUserDTO.getName(),
                requestUserDTO.getEmail(),
                requestUserDTO.getPassword()
        );

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
        user.setRoles(Set.of(userRole));
        userService.addNewUser(user);

        UserDTO response = new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Delete a user by email", description = "Deletes a user identified by their email. Admin access required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User successfully deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> removeUser(@PathVariable String email) {
        List<User> users = userService.findByEmail(email);
        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        userService.removeUser(users.get(0));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update user credentials", description = "Updates user's name, email, and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully updated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Email already exists")
    })
    @PutMapping("/{email}")
    public ResponseEntity<UserDTO> updateUserCredentials(@RequestBody UserDTO requestUserDTO,
                                                         @PathVariable String email) {

        List<User> users = userService.findByEmail(email);
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User user = users.get(0);

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
