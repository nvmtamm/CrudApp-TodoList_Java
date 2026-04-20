package com.example.todoweather.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather_cache", indexes = {
        @Index(name = "idx_city_fetched", columnList = "city, fetchedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    private LocalDateTime forecastTime;

    private Double temperature;

    private String condition;

    private Double rainChance;

    @CreationTimestamp
    private LocalDateTime fetchedAt;
}
