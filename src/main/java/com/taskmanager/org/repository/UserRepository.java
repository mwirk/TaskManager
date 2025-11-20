package com.taskmanager.org.repository;

import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findById(Integer id);
    Optional<User> findUserByEmail(String email);
    List<User> findByEmail(String email);
    Optional<User> findByEmailAndId(String email, Integer id);
    boolean existsByEmail(String email);

}
