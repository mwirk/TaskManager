package com.taskmanager.org.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.taskmanager.org.model.Category;
import com.taskmanager.org.model.Status;
import com.taskmanager.org.model.Task;
import com.taskmanager.org.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByStatus(Status status);
    List<Task> findByUserId(User user);
    List<Task> findTaskById(Integer id);
    List<Task> findByStatusAndCategoryId(Status status, Category categoryId);
    List<Task> findByCategoryId(Category categoryId);
    List<Task> findAll();
    Page<Task> findByUserId(User user, Pageable pageable);
    @Query("""
    SELECT t FROM Task t
    WHERE t.userId = :user
      AND (:title IS NULL OR LOWER(t.title) LIKE LOWER(CAST(CONCAT('%', :title, '%') AS string)))
      AND (:categoryId IS NULL OR t.categoryId.id = :categoryId)
      AND (:status IS NULL OR t.status = :status)
    """)
    Page<Task> findFiltered(@Param("user") User user,
                            @Param("title") String title,
                            @Param("categoryId") Integer categoryId,
                            @Param("status") Status status,
                            Pageable pageable);








}







