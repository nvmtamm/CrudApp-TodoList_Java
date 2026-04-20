package com.example.todoweather.config;

import com.example.todoweather.dto.response.WeatherResponse;
import com.example.todoweather.entity.Todo;
import com.example.todoweather.repository.TodoRepository;
import com.example.todoweather.repository.WeatherCacheRepository;
import com.example.todoweather.service.external.WeatherService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WeatherScheduler {

    private static final Logger logger = LoggerFactory.getLogger(WeatherScheduler.class);

    private final TodoRepository todoRepository;
    private final WeatherService weatherService;
    private final WeatherCacheRepository cacheRepository;

    /**
     * Every hour, update weather for outdoor tasks due in the next 24 hours
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void updateWeatherForUpcomingTasks() {
        logger.info("=== Weather Scheduler: Checking upcoming outdoor tasks ===");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24Hours = now.plusHours(24);

        List<Todo> upcomingTasks = todoRepository.findUpcomingOutdoorTasks(now, next24Hours);

        if (upcomingTasks.isEmpty()) {
            logger.info("No upcoming outdoor tasks found in the next 24 hours.");
            return;
        }

        logger.info("Found {} upcoming outdoor tasks to check weather", upcomingTasks.size());

        int successCount = 0;
        int failCount = 0;

        for (Todo todo : upcomingTasks) {
            try {
                WeatherResponse weather = weatherService.getCurrentWeather(todo.getLocation());

                todo.setWeatherSummary(weather.getCondition() + " - " + weather.getDescription());
                todo.setTemperature(weather.getTemperature());
                todo.setRainChance(weather.getRainChance());
                todo.setWeatherCheckedAt(LocalDateTime.now());

                // Generate suggestion
                if (weather.getRainChance() != null && weather.getRainChance() > 60) {
                    todo.setWeatherSuggestion(
                            "⚠️ Cảnh báo: Khả năng mưa " + String.format("%.0f", weather.getRainChance()) +
                                    "% tại " + todo.getLocation() + ". " +
                                    "Task '" + todo.getTitle() + "' có thể bị ảnh hưởng. Nên cân nhắc đổi lịch.");
                } else {
                    todo.setWeatherSuggestion(
                            "✅ Thời tiết thuận lợi tại " + todo.getLocation() + ": " +
                                    String.format("%.1f", weather.getTemperature()) + "°C, " +
                                    weather.getDescription());
                }

                todoRepository.save(todo);
                successCount++;

                logger.info("Updated weather for task '{}' at '{}': {}°C, rain={}%",
                        todo.getTitle(), todo.getLocation(),
                        weather.getTemperature(), weather.getRainChance());

            } catch (Exception e) {
                failCount++;
                logger.warn("Failed to update weather for task '{}': {}",
                        todo.getTitle(), e.getMessage());
            }
        }

        logger.info("=== Weather Scheduler completed: {} success, {} failed ===", successCount, failCount);
    }

    /**
     * Every 6 hours, clean up old weather cache entries (older than 24 hours)
     */
    @Scheduled(fixedRate = 21600000) // 6 hours
    @Transactional
    public void cleanupWeatherCache() {
        logger.info("Cleaning up old weather cache entries...");
        try {
            cacheRepository.deleteByFetchedAtBefore(LocalDateTime.now().minusHours(24));
            logger.info("Weather cache cleanup completed.");
        } catch (Exception e) {
            logger.warn("Failed to cleanup weather cache: {}", e.getMessage());
        }
    }
}
