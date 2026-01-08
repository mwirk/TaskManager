package com.taskmanager.org.service;

import com.taskmanager.org.exception.SameEmailException;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password123");
    }

    @Test
    void testAddNewUser_success() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        User savedUser = userService.addNewUser(user);

        assertEquals(user, savedUser);
        assertEquals("encodedPassword", savedUser.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testAddNewUser_nullUser_throwsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addNewUser(null);
        });
        assertEquals("User is null", exception.getMessage());
    }

    @Test
    void testAddNewUser_emailExists_throwsException() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        Exception exception = assertThrows(SameEmailException.class, () -> {
            userService.addNewUser(user);
        });

        assertEquals("User with this email already exists", exception.getMessage());
    }

    @Test
    void testRemoveUser() {
        userService.removeUser(user);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void testFindByEmailAndId() {
        when(userRepository.findByEmailAndId(user.getEmail(), user.getId())).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmailAndId(user.getEmail(), user.getId());

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void testFindByEmail() {
        List<User> users = new ArrayList<>();
        users.add(user);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(users);

        List<User> result = userService.findByEmail(user.getEmail());

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
    }

    @Test
    void testFindById() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(user.getId());

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void testFindAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(user);

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAllUsers();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
    }
}
