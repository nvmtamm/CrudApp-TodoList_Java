package com.example.todoweather.service;

import com.example.todoweather.dto.request.LoginRequest;
import com.example.todoweather.dto.request.RegisterRequest;
import com.example.todoweather.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
