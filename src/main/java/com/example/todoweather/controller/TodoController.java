package com.example.todoweather.controller;

import com.example.todoweather.dto.request.TodoCreateRequest;
import com.example.todoweather.dto.request.TodoStatusUpdateRequest;
import com.example.todoweather.dto.request.TodoUpdateRequest;
import com.example.todoweather.dto.response.PagedResponse;
import com.example.todoweather.dto.response.TodoResponse;
import com.example.todoweather.enums.TodoCategory;
import com.example.todoweather.enums.TodoPriority;
import com.example.todoweather.enums.TodoStatus;
import com.example.todoweather.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@Tag(name = "Todos", description = "Todo CRUD and Weather integration APIs")
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    @Operation(summary = "Create a new todo",
            description = "Create a simple todo task")
    public ResponseEntity<TodoResponse> createTodo(
            @Valid @RequestBody TodoCreateRequest request,
            Authentication authentication) {
        TodoResponse response = todoService.createTodo(request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/with-weather")
    @Operation(summary = "Create todo with weather check",
            description = "Create an outdoor todo and automatically check weather. " +
                    "Requires location and isOutdoor=true for weather data.")
    public ResponseEntity<TodoResponse> createTodoWithWeather(
            @Valid @RequestBody TodoCreateRequest request,
            Authentication authentication) {
        TodoResponse response = todoService.createTodoWithWeather(request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all todos",
            description = "Get paginated list of todos with optional filters")
    public ResponseEntity<PagedResponse<TodoResponse>> getTodos(
            Authentication authentication,
            @Parameter(description = "Filter by status") @RequestParam(required = false) TodoStatus status,
            @Parameter(description = "Filter by priority") @RequestParam(required = false) TodoPriority priority,
            @Parameter(description = "Filter by category") @RequestParam(required = false) TodoCategory category,
            @Parameter(description = "Filter outdoor tasks") @RequestParam(required = false) Boolean isOutdoor,
            @Parameter(description = "Search keyword in title/description") @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<TodoResponse> response = todoService.getTodos(
                authentication.getName(), status, priority, category, isOutdoor,
                keyword, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get todo by ID",
            description = "Get a specific todo by its ID")
    public ResponseEntity<TodoResponse> getTodoById(
            @PathVariable Long id,
            Authentication authentication) {
        TodoResponse response = todoService.getTodoById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a todo",
            description = "Update todo fields (only non-null fields will be updated)")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody TodoUpdateRequest request,
            Authentication authentication) {
        TodoResponse response = todoService.updateTodo(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update todo status",
            description = "Change the status of a todo (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)")
    public ResponseEntity<TodoResponse> updateTodoStatus(
            @PathVariable Long id,
            @Valid @RequestBody TodoStatusUpdateRequest request,
            Authentication authentication) {
        TodoResponse response = todoService.updateTodoStatus(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a todo",
            description = "Permanently delete a todo")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable Long id,
            Authentication authentication) {
        todoService.deleteTodo(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/weather-check")
    @Operation(summary = "Check weather for a todo",
            description = "Fetch or refresh weather data for a todo with a location")
    public ResponseEntity<TodoResponse> checkWeatherForTodo(
            @PathVariable Long id,
            Authentication authentication) {
        TodoResponse response = todoService.checkWeatherForTodo(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
