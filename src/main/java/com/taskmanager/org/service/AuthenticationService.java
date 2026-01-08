package com.taskmanager.org.service;

import com.taskmanager.org.dto.LoginUserDTO;
import com.taskmanager.org.exception.UserNotFoundException;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }



    public User authenticate(LoginUserDTO input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );
        List<User> user = userRepository.findByEmail(input.getEmail());
        if (user.isEmpty()){
            throw new UserNotFoundException("User not found");
        }
        return user.getFirst();
    }
}