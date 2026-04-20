package com.example.todoweather.controller;

import com.example.todoweather.dto.response.ForecastResponse;
import com.example.todoweather.dto.response.WeatherResponse;
import com.example.todoweather.service.external.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Tag(name = "Weather", description = "Weather data APIs powered by OpenWeatherMap")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/current")
    @Operation(summary = "Get current weather",
            description = "Get current weather conditions for a city. Example: DaNang, HoChiMinh, Hanoi")
    public ResponseEntity<WeatherResponse> getCurrentWeather(
            @Parameter(description = "City name", example = "DaNang")
            @RequestParam String city) {
        WeatherResponse response = weatherService.getCurrentWeather(city);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/forecast")
    @Operation(summary = "Get weather forecast",
            description = "Get 5-day / 3-hour weather forecast for a city")
    public ResponseEntity<ForecastResponse> getForecast(
            @Parameter(description = "City name", example = "DaNang")
            @RequestParam String city) {
        ForecastResponse response = weatherService.getForecast(city);
        return ResponseEntity.ok(response);
    }
}
