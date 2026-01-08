package com.taskmanager.org.restControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.org.controller.AuthRestController;
import com.taskmanager.org.dto.LoginResponse;
import com.taskmanager.org.dto.LoginUserDTO;
import com.taskmanager.org.exception.GlobalExceptionHandler;
import com.taskmanager.org.model.Role;
import com.taskmanager.org.model.User;
import com.taskmanager.org.service.AuthenticationService;
import com.taskmanager.org.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthRestControllerTest {

    private MockMvc mockMvc;
    private AuthRestController authRestController;
    private AuthenticationService authenticationService;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authenticationService = Mockito.mock(AuthenticationService.class);
        jwtService = Mockito.mock(JwtService.class);
        authenticationManager = Mockito.mock(AuthenticationManager.class);

        authRestController = new AuthRestController(authenticationManager, jwtService, authenticationService);

        mockMvc = MockMvcBuilders.standaloneSetup(authRestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void testAuthenticate_Success() throws Exception {
        LoginUserDTO loginDto = new LoginUserDTO();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password");


        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Test User");
        mockUser.setEmail("test@example.com");
        mockUser.setRoles(Set.of(new Role("ROLE_USER")));

        when(authenticationService.authenticate(any(LoginUserDTO.class))).thenReturn(mockUser);
        when(jwtService.generateToken(any())).thenReturn("fake-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void testAuthenticate_Failure_InvalidCredentials() throws Exception {
        LoginUserDTO loginDto = new LoginUserDTO();
        loginDto.setEmail("wrong@example.com");
        loginDto.setPassword("wrong");


        when(authenticationService.authenticate(any(LoginUserDTO.class)))
                .thenThrow(new IllegalArgumentException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid login credentials"));
    }

    @Test
    void testAuthenticate_Failure_OtherException() throws Exception {
        LoginUserDTO loginDto = new LoginUserDTO();
        loginDto.setEmail("error@example.com");
        loginDto.setPassword("error");


        when(authenticationService.authenticate(any(LoginUserDTO.class)))
                .thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }
}
