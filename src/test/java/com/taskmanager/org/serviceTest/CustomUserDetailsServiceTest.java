package com.taskmanager.org.serviceTest;

import com.taskmanager.org.model.Role;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.UserRepository;
import com.taskmanager.org.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private CustomUserDetailsService customUserDetailsService;

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        customUserDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void shouldLoadUserByEmail() {

        User user = new User();
        user.setEmail("test@mail.com");
        user.setPassword("encodedPassword");

        Role roleUser = new Role();
        roleUser.setName("ROLE_USER");

        Role roleAdmin = new Role();
        roleAdmin.setName("ROLE_ADMIN");

        user.setRoles(Set.of(roleUser, roleAdmin));

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(List.of(user));


        UserDetails result = customUserDetailsService.loadUserByUsername("test@mail.com");


        assertNotNull(result);
        assertEquals("test@mail.com", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        verify(userRepository).findByEmail("test@mail.com");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("notfound@mail.com"))
                .thenReturn(List.of());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("notfound@mail.com"));

        verify(userRepository).findByEmail("notfound@mail.com");
    }

    @Test
    void shouldMapRolesToAuthorities() {
        User user = new User();
        user.setEmail("roles@mail.com");
        user.setPassword("pwd");

        Role role = new Role();
        role.setName("ROLE_MANAGER");

        user.setRoles(Set.of(role));

        when(userRepository.findByEmail("roles@mail.com"))
                .thenReturn(List.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("roles@mail.com");

        assertEquals(1, result.getAuthorities().size());
        assertEquals("ROLE_MANAGER",
                result.getAuthorities().iterator().next().getAuthority());
    }
}
