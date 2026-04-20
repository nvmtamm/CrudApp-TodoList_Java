package com.example.todoweather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResponse {

    private String city;
    private List<ForecastItem> forecasts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastItem {
        private LocalDateTime dateTime;
        private Double temperature;
        private Double feelsLike;
        private Double tempMin;
        private Double tempMax;
        private Integer humidity;
        private String condition;
        private String description;
        private String icon;
        private Double windSpeed;
        private Double rainChance;
        private Double rainVolume;
    }
}
