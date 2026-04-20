package com.example.todoweather.mapper;

import com.example.todoweather.dto.request.TodoCreateRequest;
import com.example.todoweather.dto.response.TodoResponse;
import com.example.todoweather.entity.Todo;
import com.example.todoweather.entity.User;
import com.example.todoweather.enums.TodoCategory;
import com.example.todoweather.enums.TodoPriority;
import org.springframework.stereotype.Component;

@Component
public class TodoMapper {

    /**
     * Convert TodoCreateRequest to Todo entity
     */
    public Todo toEntity(TodoCreateRequest request, User user) {
        return Todo.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : TodoPriority.MEDIUM)
                .category(request.getCategory() != null ? request.getCategory() : TodoCategory.OTHER)
                .dueDateTime(request.getDueDateTime())
                .location(request.getLocation())
                .isOutdoor(request.getIsOutdoor() != null ? request.getIsOutdoor() : false)
                .user(user)
                .build();
    }

    /**
     * Convert Todo entity to TodoResponse
     */
    public TodoResponse toResponse(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .status(todo.getStatus())
                .priority(todo.getPriority())
                .category(todo.getCategory())
                .dueDateTime(todo.getDueDateTime())
                .location(todo.getLocation())
                .isOutdoor(todo.getIsOutdoor())
                .weatherSummary(todo.getWeatherSummary())
                .temperature(todo.getTemperature())
                .rainChance(todo.getRainChance())
                .weatherCheckedAt(todo.getWeatherCheckedAt())
                .weatherSuggestion(todo.getWeatherSuggestion())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
}
