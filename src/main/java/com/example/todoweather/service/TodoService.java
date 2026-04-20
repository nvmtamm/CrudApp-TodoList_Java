package com.example.todoweather.service;

import com.example.todoweather.dto.request.TodoCreateRequest;
import com.example.todoweather.dto.request.TodoStatusUpdateRequest;
import com.example.todoweather.dto.request.TodoUpdateRequest;
import com.example.todoweather.dto.response.PagedResponse;
import com.example.todoweather.dto.response.TodoResponse;
import com.example.todoweather.enums.TodoCategory;
import com.example.todoweather.enums.TodoPriority;
import com.example.todoweather.enums.TodoStatus;

public interface TodoService {

    TodoResponse createTodo(TodoCreateRequest request, String username);

    TodoResponse createTodoWithWeather(TodoCreateRequest request, String username);

    TodoResponse getTodoById(Long id, String username);

    PagedResponse<TodoResponse> getTodos(String username, TodoStatus status, TodoPriority priority,
                                          TodoCategory category, Boolean isOutdoor,
                                          String keyword, int page, int size, String sortBy, String sortDir);

    TodoResponse updateTodo(Long id, TodoUpdateRequest request, String username);

    TodoResponse updateTodoStatus(Long id, TodoStatusUpdateRequest request, String username);

    void deleteTodo(Long id, String username);

    TodoResponse checkWeatherForTodo(Long id, String username);
}
