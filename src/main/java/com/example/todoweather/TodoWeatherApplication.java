package com.example.todoweather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TodoWeatherApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoWeatherApplication.class, args);
    }

}
