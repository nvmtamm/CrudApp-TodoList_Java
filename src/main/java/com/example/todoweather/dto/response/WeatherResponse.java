package com.example.todoweather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {

    private String city;
    private Double temperature;
    private Double feelsLike;
    private Double tempMin;
    private Double tempMax;
    private Integer humidity;
    private String condition;
    private String description;
    private String icon;
    private Double windSpeed;
    private Double rainChance;      // Probability of precipitation (0-100%)
    private Double rainVolume;      // Rain volume in mm (last 1h or 3h)
    private LocalDateTime fetchedAt;
    private String suggestion;      // Weather-based suggestion
}
