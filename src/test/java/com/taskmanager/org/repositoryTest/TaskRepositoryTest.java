package com.taskmanager.org.repositoryTest;

import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Status;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.CategoryRepository;
import com.taskmanager.org.repository.TaskRepository;
import com.taskmanager.org.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user1;
    private User user2;
    private Category cat1;
    private Category cat2;

    @BeforeEach
    void setUp() {

        user1 = new User();
        user1.setName("User One");
        user1.setEmail("user1@example.com");
        user1.setPassword("pass");
        userRepository.save(user1);

        user2 = new User();
        user2.setName("User Two");
        user2.setEmail("user2@example.com");
        user2.setPassword("pass");

        userRepository.save(user2);


        cat1 = new Category();
        cat1.setName("Cat1");
        cat1.setColor("sd");
        categoryRepository.save(cat1);

        cat2 = new Category();
        cat2.setName("Cat2");
        cat2.setColor("sd");
        categoryRepository.save(cat2);


        Task t1 = new Task();
        t1.setTitle("Task One");
        t1.setDescription("First task");
        t1.setStatus(Status.TO_DO);
        t1.setDueDate(LocalDateTime.now().plusDays(1));
        t1.setCreatedAt(LocalDateTime.now().minusDays(1));
        t1.setUpdatedAt(LocalDateTime.now());
        t1.setUserId(user1);
        t1.setCategoryId(cat1);
        taskRepository.save(t1);

        Task t2 = new Task();
        t2.setTitle("Task Two");
        t2.setDescription("Second task");
        t2.setStatus(Status.DONE);
        t2.setDueDate(LocalDateTime.now().plusDays(2));
        t2.setCreatedAt(LocalDateTime.now().minusDays(2));
        t2.setUpdatedAt(LocalDateTime.now());
        t2.setUserId(user1);
        t2.setCategoryId(cat2);
        taskRepository.save(t2);

        Task t3 = new Task();
        t3.setTitle("Other User Task");
        t3.setDescription("Task by user2");
        t3.setStatus(Status.IN_PROGRESS);
        t3.setDueDate(LocalDateTime.now().plusDays(3));
        t3.setCreatedAt(LocalDateTime.now().minusDays(3));
        t3.setUpdatedAt(LocalDateTime.now());
        t3.setUserId(user2);
        t3.setCategoryId(cat1);
        taskRepository.save(t3);
    }

    @Test
    void findFiltered_ByUserOnly() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = taskRepository.findFiltered(user1, null, null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        page.getContent().forEach(t -> assertThat(t.getUserId()).isEqualTo(user1));
    }

    @Test
    void findFiltered_ByUserAndTitle() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = taskRepository.findFiltered(user1, "one", null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Task One");
    }

    @Test
    void findFiltered_ByCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = taskRepository.findFiltered(user1, null, cat2.getId(), null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getCategoryId()).isEqualTo(cat2);
    }

    @Test
    void findFiltered_ByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = taskRepository.findFiltered(user1, null, null, Status.DONE, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    void findFiltered_NoMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = taskRepository.findFiltered(user1, "NonExisting", null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(0);
    }
}
