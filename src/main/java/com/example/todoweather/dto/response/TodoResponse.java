package com.example.todoweather.dto.response;

import com.example.todoweather.enums.TodoCategory;
import com.example.todoweather.enums.TodoPriority;
import com.example.todoweather.enums.TodoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoResponse {

    private Long id;
    private String title;
    private String description;
    private TodoStatus status;
    private TodoPriority priority;
    private TodoCategory category;
    private LocalDateTime dueDateTime;
    private String location;
    private Boolean isOutdoor;

    // Weather info
    private String weatherSummary;
    private Double temperature;
    private Double rainChance;
    private LocalDateTime weatherCheckedAt;
    private String weatherSuggestion;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
