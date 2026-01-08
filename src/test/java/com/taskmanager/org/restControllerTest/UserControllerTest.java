package com.taskmanager.org.restControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.org.controller.UserController;
import com.taskmanager.org.dto.UserDTO;
import com.taskmanager.org.model.Role;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.RoleRepository;
import com.taskmanager.org.service.CategoryService;
import com.taskmanager.org.service.JwtService;
import com.taskmanager.org.service.TaskService;
import com.taskmanager.org.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.taskmanager.org.exception.GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private TaskService taskService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;


    @Test
    void shouldReturnAllUsers() throws Exception {
        User user1 = new User(1L, "Alice", "alice@test.com", "pass1");
        User user2 = new User(2L, "Bob", "bob@test.com", "pass2");

        when(userService.findAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].email").value("bob@test.com"));

        verify(userService).findAllUsers();
    }

    @Test
    void shouldReturnFilteredUserByEmail() throws Exception {
        User user = new User(1L, "Alice", "alice@test.com", "pass1");

        when(userService.findByEmail("alice@test.com")).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/users")
                        .param("email", "alice@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].email").value("alice@test.com"));
    }

    @Test
    void shouldCreateNewUser() throws Exception {
        UserDTO request = new UserDTO(null, "Charlie", "charlie@test.com");
        User user = new User(null, "Charlie", "charlie@test.com", "pass3");

        Role userRole = new Role("ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userService.addNewUser(any(User.class))).thenReturn(user);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Charlie"))
                .andExpect(jsonPath("$.email").value("charlie@test.com"));

        verify(userService).addNewUser(any(User.class));
        verify(roleRepository).findByName("ROLE_USER");
    }

    @Test
    void shouldDeleteExistingUser() throws Exception {
        User user = new User(1L, "Alice", "alice@test.com", "pass1");

        when(userService.findByEmail("alice@test.com")).thenReturn(List.of(user));
        doNothing().when(userService).removeUser(user);

        mockMvc.perform(delete("/api/v1/users/{email}", "alice@test.com"))
                .andExpect(status().isNoContent());

        verify(userService).removeUser(user);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingUser() throws Exception {
        when(userService.findByEmail("missing@test.com")).thenReturn(List.of());

        mockMvc.perform(delete("/api/v1/users/{email}", "missing@test.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateUserCredentials() throws Exception {
        User existingUser = new User(1L, "Alice", "alice@test.com", "oldpass");
        UserDTO request = new UserDTO(null, "Alice Updated", "alice@test.com");

        when(userService.findByEmail("alice@test.com")).thenReturn(List.of(existingUser));

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/users/{email}", "alice@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"))
                .andExpect(jsonPath("$.email").value("alice@test.com"));
    }


}
