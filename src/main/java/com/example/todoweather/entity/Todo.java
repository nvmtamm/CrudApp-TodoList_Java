package com.example.todoweather.entity;

import com.example.todoweather.enums.TodoCategory;
import com.example.todoweather.enums.TodoPriority;
import com.example.todoweather.enums.TodoStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String title;

    @Size(max = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TodoStatus status = TodoStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TodoPriority priority = TodoPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TodoCategory category = TodoCategory.OTHER;

    private LocalDateTime dueDateTime;

    @Size(max = 200)
    private String location;

    @Builder.Default
    private Boolean isOutdoor = false;

    // Weather information attached to the task
    private String weatherSummary;

    private Double temperature;

    private Double rainChance;

    private LocalDateTime weatherCheckedAt;

    // Weather suggestion message
    @Size(max = 500)
    private String weatherSuggestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
