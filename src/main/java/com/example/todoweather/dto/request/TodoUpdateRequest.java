package com.example.todoweather.dto.request;

import com.example.todoweather.enums.TodoCategory;
import com.example.todoweather.enums.TodoPriority;
import com.example.todoweather.enums.TodoStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoUpdateRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private TodoStatus status;

    private TodoPriority priority;

    private TodoCategory category;

    private LocalDateTime dueDateTime;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    private Boolean isOutdoor;
}
