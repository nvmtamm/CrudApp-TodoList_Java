package com.example.todoweather.repository;

import com.example.todoweather.entity.Todo;
import com.example.todoweather.enums.TodoCategory;
import com.example.todoweather.enums.TodoPriority;
import com.example.todoweather.enums.TodoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    // Find all todos by user with pagination
    Page<Todo> findByUserId(Long userId, Pageable pageable);

    // Find todo by id and user id (ownership check)
    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    // Filter by status
    Page<Todo> findByUserIdAndStatus(Long userId, TodoStatus status, Pageable pageable);

    // Filter by priority
    Page<Todo> findByUserIdAndPriority(Long userId, TodoPriority priority, Pageable pageable);

    // Filter by category
    Page<Todo> findByUserIdAndCategory(Long userId, TodoCategory category, Pageable pageable);

    // Filter by outdoor
    Page<Todo> findByUserIdAndIsOutdoor(Long userId, Boolean isOutdoor, Pageable pageable);

    // Search by keyword in title or description
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId " +
            "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Todo> searchByKeyword(@Param("userId") Long userId,
                                @Param("keyword") String keyword,
                                Pageable pageable);

    // Combined filter query
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:category IS NULL OR t.category = :category) " +
            "AND (:isOutdoor IS NULL OR t.isOutdoor = :isOutdoor) " +
            "AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR :keyword IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Todo> findWithFilters(@Param("userId") Long userId,
                               @Param("status") TodoStatus status,
                               @Param("priority") TodoPriority priority,
                               @Param("category") TodoCategory category,
                               @Param("isOutdoor") Boolean isOutdoor,
                               @Param("keyword") String keyword,
                               Pageable pageable);

    // Find outdoor tasks with due date in next N hours (for scheduler)
    @Query("SELECT t FROM Todo t WHERE t.isOutdoor = true " +
            "AND t.status != 'COMPLETED' AND t.status != 'CANCELLED' " +
            "AND t.location IS NOT NULL " +
            "AND t.dueDateTime BETWEEN :now AND :until")
    List<Todo> findUpcomingOutdoorTasks(@Param("now") LocalDateTime now,
                                        @Param("until") LocalDateTime until);
}
