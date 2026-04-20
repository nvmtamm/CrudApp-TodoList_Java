package com.example.todoweather.dto.request;

import com.example.todoweather.enums.TodoStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private TodoStatus status;
}
