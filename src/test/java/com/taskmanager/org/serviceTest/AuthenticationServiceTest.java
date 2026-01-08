package com.taskmanager.org.serviceTest;


import com.taskmanager.org.dto.LoginUserDTO;
import com.taskmanager.org.exception.UserNotFoundException;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.UserRepository;
import com.taskmanager.org.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    private AuthenticationService authenticationService;

    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authenticationManager = mock(AuthenticationManager.class);
        passwordEncoder = mock(org.springframework.security.crypto.password.PasswordEncoder.class);

        authenticationService = new AuthenticationService(
                userRepository,
                authenticationManager,
                passwordEncoder
        );
    }

    @Test
    void shouldAuthenticateAndReturnUser() {

        LoginUserDTO dto = new LoginUserDTO("test@mail.com", "password");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");


        when(authenticationManager.authenticate(
                ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(null);


        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(List.of(user));


        User result = authenticationService.authenticate(dto);


        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@mail.com", result.getEmail());

        verify(authenticationManager).authenticate(
                ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)
        );
        verify(userRepository).findByEmail("test@mail.com");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        LoginUserDTO dto = new LoginUserDTO("none@mail.com", "pass");


        when(authenticationManager.authenticate(any())).thenReturn(null);


        when(userRepository.findByEmail("none@mail.com"))
                .thenReturn(List.of());

        assertThrows(UserNotFoundException.class,
                () -> authenticationService.authenticate(dto));

        verify(userRepository).findByEmail("none@mail.com");
    }

    @Test
    void shouldPropagateAuthenticationException() {
        LoginUserDTO dto = new LoginUserDTO("mail@mail.com", "wrongpass");


        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authenticationService.authenticate(dto));

        verify(authenticationManager).authenticate(any());
        verify(userRepository, never()).findByEmail(any());
    }
}

