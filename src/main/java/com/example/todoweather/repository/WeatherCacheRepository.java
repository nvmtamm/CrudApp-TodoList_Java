package com.example.todoweather.repository;

import com.example.todoweather.entity.WeatherCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface WeatherCacheRepository extends JpaRepository<WeatherCache, Long> {

    // Find cached weather for a city that was fetched recently
    Optional<WeatherCache> findFirstByCityIgnoreCaseAndFetchedAtAfterOrderByFetchedAtDesc(
            String city, LocalDateTime after);

    // Delete old cache entries
    void deleteByFetchedAtBefore(LocalDateTime before);
}
