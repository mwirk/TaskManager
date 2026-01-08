package com.taskmanager.org.service;

import com.taskmanager.org.exception.SameEmailException;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public User addNewUser(User newUser){
        if (newUser == null){
            throw new IllegalArgumentException("User is null");
        }
        List<User> users = userRepository.findAll();
        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new SameEmailException("User with this email already exists");
        }

         newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        return userRepository.save(newUser);
    }
    @Transactional
    public void removeUser(User user){
        userRepository.delete(user);
    }
    @Transactional(readOnly = true)
    public Optional<User> findByEmailAndId(String email, Long id) {
        return userRepository.findByEmailAndId(email,id);
    }
    @Transactional(readOnly = true)
    public List<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
