package com.taskmanager.org.service;

import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public User addNewUser(User newUser){
        if (newUser == null){
            throw new IllegalArgumentException("User is null");
        }
        List<User> users = userRepository.findAll();
        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

         newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        return userRepository.save(newUser);
    }
    public void removeUser(User user){
        userRepository.delete(user);
    }

    public Optional<User> findByEmailAndId(String email, Integer id) {
        return userRepository.findByEmailAndId(email,id);
    }

    public List<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
