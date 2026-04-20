package com.example.todoweather.service.external;

import com.example.todoweather.dto.response.ForecastResponse;
import com.example.todoweather.dto.response.WeatherResponse;
import com.example.todoweather.entity.WeatherCache;
import com.example.todoweather.repository.WeatherCacheRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final WebClient webClient;
    private final WeatherCacheRepository cacheRepository;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.base-url}")
    private String baseUrl;

    @Value("${weather.api.cache-duration-minutes}")
    private int cacheDurationMinutes;

    /**
     * Get current weather for a city (with caching)
     */
    public WeatherResponse getCurrentWeather(String city) {
        // Check cache first
        Optional<WeatherCache> cached = cacheRepository
                .findFirstByCityIgnoreCaseAndFetchedAtAfterOrderByFetchedAtDesc(
                        city, LocalDateTime.now().minusMinutes(cacheDurationMinutes));

        if (cached.isPresent()) {
            logger.debug("Returning cached weather for '{}'", city);
            WeatherCache cache = cached.get();
            return WeatherResponse.builder()
                    .city(cache.getCity())
                    .temperature(cache.getTemperature())
                    .condition(cache.getCondition())
                    .rainChance(cache.getRainChance())
                    .fetchedAt(cache.getFetchedAt())
                    .build();
        }

        // Call OpenWeatherMap API
        String url = String.format("%s/weather?q=%s&appid=%s&units=metric&lang=vi",
                baseUrl, city, apiKey);

        try {
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from weather API");
            }

            WeatherResponse weather = parseCurrentWeather(response, city);

            // Save to cache
            saveToCache(city, weather);

            logger.info("Fetched current weather for '{}': {}°C, {}",
                    city, weather.getTemperature(), weather.getCondition());

            return weather;
        } catch (WebClientResponseException e) {
            logger.error("Weather API error for '{}': {} - {}", city, e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 404) {
                throw new RuntimeException("City '" + city + "' not found. Please check the city name.");
            }
            throw new RuntimeException("Failed to fetch weather data: " + e.getMessage());
        }
    }

    /**
     * Get 5-day forecast for a city
     */
    public ForecastResponse getForecast(String city) {
        String url = String.format("%s/forecast?q=%s&appid=%s&units=metric&lang=vi",
                baseUrl, city, apiKey);

        try {
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from weather API");
            }

            return parseForecast(response, city);

        } catch (WebClientResponseException e) {
            logger.error("Forecast API error for '{}': {} - {}", city, e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 404) {
                throw new RuntimeException("City '" + city + "' not found. Please check the city name.");
            }
            throw new RuntimeException("Failed to fetch forecast data: " + e.getMessage());
        }
    }

    /**
     * Parse current weather JSON response from OpenWeatherMap
     */
    private WeatherResponse parseCurrentWeather(JsonNode json, String city) {
        JsonNode main = json.path("main");
        JsonNode weather = json.path("weather").get(0);
        JsonNode wind = json.path("wind");
        JsonNode rain = json.path("rain");
        JsonNode clouds = json.path("clouds");

        // Calculate rain chance: use clouds as proxy if no rain probability
        double rainChance = 0;
        if (rain.has("1h") && rain.get("1h").asDouble() > 0) {
            rainChance = Math.min(rain.get("1h").asDouble() * 20, 100); // estimate
        } else if (clouds.has("all")) {
            rainChance = clouds.get("all").asDouble() * 0.5; // rough estimate from cloud coverage
        }

        String condition = weather.path("main").asText("");
        if (condition.equalsIgnoreCase("Rain") || condition.equalsIgnoreCase("Drizzle")) {
            rainChance = Math.max(rainChance, 80);
        } else if (condition.equalsIgnoreCase("Thunderstorm")) {
            rainChance = Math.max(rainChance, 90);
        }

        return WeatherResponse.builder()
                .city(city)
                .temperature(main.path("temp").asDouble())
                .feelsLike(main.path("feels_like").asDouble())
                .tempMin(main.path("temp_min").asDouble())
                .tempMax(main.path("temp_max").asDouble())
                .humidity(main.path("humidity").asInt())
                .condition(condition)
                .description(weather.path("description").asText(""))
                .icon(weather.path("icon").asText(""))
                .windSpeed(wind.path("speed").asDouble())
                .rainChance(rainChance)
                .rainVolume(rain.has("1h") ? rain.get("1h").asDouble() : 0)
                .fetchedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Parse forecast JSON response from OpenWeatherMap
     */
    private ForecastResponse parseForecast(JsonNode json, String city) {
        JsonNode list = json.path("list");
        List<ForecastResponse.ForecastItem> items = new ArrayList<>();

        for (JsonNode item : list) {
            JsonNode main = item.path("main");
            JsonNode weather = item.path("weather").get(0);
            JsonNode wind = item.path("wind");
            JsonNode rain = item.path("rain");
            JsonNode pop = item.path("pop"); // Probability of precipitation (0-1)

            double rainChance = pop.isMissingNode() ? 0 : pop.asDouble() * 100;

            long dt = item.path("dt").asLong();
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(dt), ZoneId.systemDefault());

            items.add(ForecastResponse.ForecastItem.builder()
                    .dateTime(dateTime)
                    .temperature(main.path("temp").asDouble())
                    .feelsLike(main.path("feels_like").asDouble())
                    .tempMin(main.path("temp_min").asDouble())
                    .tempMax(main.path("temp_max").asDouble())
                    .humidity(main.path("humidity").asInt())
                    .condition(weather.path("main").asText(""))
                    .description(weather.path("description").asText(""))
                    .icon(weather.path("icon").asText(""))
                    .windSpeed(wind.path("speed").asDouble())
                    .rainChance(rainChance)
                    .rainVolume(rain != null && rain.has("3h") ? rain.get("3h").asDouble() : 0)
                    .build());
        }

        return ForecastResponse.builder()
                .city(city)
                .forecasts(items)
                .build();
    }

    /**
     * Save weather data to cache
     */
    private void saveToCache(String city, WeatherResponse weather) {
        try {
            WeatherCache cache = WeatherCache.builder()
                    .city(city)
                    .temperature(weather.getTemperature())
                    .condition(weather.getCondition())
                    .rainChance(weather.getRainChance())
                    .build();
            cacheRepository.save(cache);
        } catch (Exception e) {
            logger.warn("Failed to cache weather data: {}", e.getMessage());
        }
    }
}
