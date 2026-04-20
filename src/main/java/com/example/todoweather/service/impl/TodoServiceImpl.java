package com.example.todoweather.service.impl;

import com.example.todoweather.dto.request.TodoCreateRequest;
import com.example.todoweather.dto.request.TodoStatusUpdateRequest;
import com.example.todoweather.dto.request.TodoUpdateRequest;
import com.example.todoweather.dto.response.PagedResponse;
import com.example.todoweather.dto.response.TodoResponse;
import com.example.todoweather.dto.response.WeatherResponse;
import com.example.todoweather.entity.Todo;
import com.example.todoweather.entity.User;
import com.example.todoweather.enums.TodoCategory;
import com.example.todoweather.enums.TodoPriority;
import com.example.todoweather.enums.TodoStatus;
import com.example.todoweather.exception.BadRequestException;
import com.example.todoweather.exception.ResourceNotFoundException;
import com.example.todoweather.mapper.TodoMapper;
import com.example.todoweather.repository.TodoRepository;
import com.example.todoweather.security.CustomUserDetailsService;
import com.example.todoweather.service.TodoService;
import com.example.todoweather.service.external.WeatherService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private static final Logger logger = LoggerFactory.getLogger(TodoServiceImpl.class);
    private static final double RAIN_THRESHOLD = 60.0;

    private final TodoRepository todoRepository;
    private final CustomUserDetailsService userDetailsService;
    private final TodoMapper todoMapper;
    private final WeatherService weatherService;

    @Override
    @Transactional
    public TodoResponse createTodo(TodoCreateRequest request, String username) {
        User user = userDetailsService.loadUserEntityByUsername(username);
        Todo todo = todoMapper.toEntity(request, user);
        Todo saved = todoRepository.save(todo);
        logger.info("Created todo '{}' for user '{}'", saved.getTitle(), username);
        return todoMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TodoResponse createTodoWithWeather(TodoCreateRequest request, String username) {
        User user = userDetailsService.loadUserEntityByUsername(username);
        Todo todo = todoMapper.toEntity(request, user);

        // If outdoor task with location, fetch weather
        if (Boolean.TRUE.equals(todo.getIsOutdoor()) && todo.getLocation() != null && !todo.getLocation().isBlank()) {
            attachWeatherToTodo(todo);
        }

        Todo saved = todoRepository.save(todo);
        logger.info("Created todo '{}' with weather check for user '{}'", saved.getTitle(), username);
        return todoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TodoResponse getTodoById(Long id, String username) {
        User user = userDetailsService.loadUserEntityByUsername(username);
        Todo todo = todoRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));
        return todoMapper.toResponse(todo);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TodoResponse> getTodos(String username, TodoStatus status,
                                                 TodoPriority priority, TodoCategory category,
                                                 Boolean isOutdoor, String keyword,
                                                 int page, int size, String sortBy, String sortDir) {
        User user = userDetailsService.loadUserEntityByUsername(username);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Todo> todoPage;

        // Use combined filter query if any filter is specified
        if (status != null || priority != null || category != null || isOutdoor != null || keyword != null) {
            todoPage = todoRepository.findWithFilters(
                    user.getId(), status, priority, category, isOutdoor, keyword, pageable);
        } else {
            todoPage = todoRepository.findByUserId(user.getId(), pageable);
        }

        List<TodoResponse> content = todoPage.getContent().stream()
                .map(todoMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<TodoResponse>builder()
                .content(content)
                .page(todoPage.getNumber())
                .size(todoPage.getSize())
                .totalElements(todoPage.getTotalElements())
                .totalPages(todoPage.getTotalPages())
                .last(todoPage.isLast())
                .first(todoPage.isFirst())
                .build();
    }

    @Override
    @Transactional
    public TodoResponse updateTodo(Long id, TodoUpdateRequest request, String username) {
        User user = userDetailsService.loadUserEntityByUsername(username);
        Todo todo = todoRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));

        // Update only non-null fields
        if (request.getTitle() != null) todo.setTitle(request.getTitle());
        if (request.getDescription() != null) todo.setDescription(request.getDescription());
        if (request.getStatus() != null) todo.setStatus(request.getStatus());
        if (request.getPriority() != null) todo.setPriority(request.getPriority());
        if (request.getCategory() != null) todo.setCategory(request.getCategory());
        if (request.getDueDateTime() != null) todo.setDueDateTime(request.getDueDateTime());
        if (request.getLocation() != null) todo.setLocation(request.getLocation());
        if (request.getIsOutdoor() != null) todo.setIsOutdoor(request.getIsOutdoor());

        Todo updated = todoRepository.save(todo);
        logger.info("Updated todo '{}' for user '{}'", updated.getTitle(), username);
        return todoMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public TodoResponse updateTodoStatus(Long id, TodoStatusUpdateRequest request, String username) {
        User user = userDetailsService.loadUserEntityByUsername(username);
        Todo todo = todoRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));

        todo.setStatus(request.getStatus());
        Todo updated = todoRepository.save(todo);
        logger.info("Updated todo '{}' status to '{}' for user '{}'",
                updated.getTitle(), request.getStatus(), username);
        return todoMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTodo(Long id, String username) {
        User user = userDetailsService.loadUserEntityByUsername(username);
        Todo todo = todoRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));
        todoRepository.delete(todo);
        logger.info("Deleted todo '{}' for user '{}'", todo.getTitle(), username);
    }

    @Override
    @Transactional
    public TodoResponse checkWeatherForTodo(Long id, String username) {
        User user = userDetailsService.loadUserEntityByUsername(username);
        Todo todo = todoRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));

        if (todo.getLocation() == null || todo.getLocation().isBlank()) {
            throw new BadRequestException("Todo does not have a location set. Please update the location first.");
        }

        attachWeatherToTodo(todo);
        Todo updated = todoRepository.save(todo);
        return todoMapper.toResponse(updated);
    }

    /**
     * Attach weather information to a todo task
     */
    private void attachWeatherToTodo(Todo todo) {
        try {
            WeatherResponse weather = weatherService.getCurrentWeather(todo.getLocation());

            todo.setWeatherSummary(weather.getCondition() + " - " + weather.getDescription());
            todo.setTemperature(weather.getTemperature());
            todo.setRainChance(weather.getRainChance());
            todo.setWeatherCheckedAt(LocalDateTime.now());

            // Generate weather suggestion
            String suggestion = generateWeatherSuggestion(weather);
            todo.setWeatherSuggestion(suggestion);

            logger.info("Attached weather data for '{}': temp={}, rain={}%",
                    todo.getLocation(), weather.getTemperature(), weather.getRainChance());
        } catch (Exception e) {
            logger.warn("Failed to fetch weather for location '{}': {}", todo.getLocation(), e.getMessage());
            todo.setWeatherSuggestion("Unable to fetch weather data: " + e.getMessage());
        }
    }

    /**
     * Generate a weather-based suggestion for the task
     */
    private String generateWeatherSuggestion(WeatherResponse weather) {
        StringBuilder suggestion = new StringBuilder();

        if (weather.getRainChance() != null && weather.getRainChance() > RAIN_THRESHOLD) {
            suggestion.append("⚠️ Khả năng mưa cao (")
                    .append(String.format("%.0f", weather.getRainChance()))
                    .append("%), nên đổi lịch hoặc chuẩn bị áo mưa. ");
        } else if (weather.getRainChance() != null && weather.getRainChance() > 30) {
            suggestion.append("🌦️ Có thể có mưa nhẹ (")
                    .append(String.format("%.0f", weather.getRainChance()))
                    .append("%), nên mang ô phòng khi. ");
        }

        if (weather.getTemperature() != null) {
            if (weather.getTemperature() > 35) {
                suggestion.append("🌡️ Nhiệt độ cao (")
                        .append(String.format("%.1f", weather.getTemperature()))
                        .append("°C), nhớ uống nhiều nước. ");
            } else if (weather.getTemperature() < 15) {
                suggestion.append("🥶 Nhiệt độ thấp (")
                        .append(String.format("%.1f", weather.getTemperature()))
                        .append("°C), nhớ mặc ấm. ");
            }
        }

        if (weather.getWindSpeed() != null && weather.getWindSpeed() > 10) {
            suggestion.append("💨 Gió mạnh (")
                    .append(String.format("%.1f", weather.getWindSpeed()))
                    .append(" m/s). ");
        }

        if (suggestion.length() == 0) {
            suggestion.append("✅ Thời tiết thuận lợi cho hoạt động ngoài trời.");
        }

        return suggestion.toString().trim();
    }
}
