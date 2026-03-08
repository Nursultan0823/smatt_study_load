package com.example.smatt_study_load.controllers;

import org.springframework.web.bind.annotation.*;

import com.example.smatt_study_load.DTO.AuthResponse;
import com.example.smatt_study_load.DTO.LoginRequest;
import com.example.smatt_study_load.DTO.RegisterRequest;
import com.example.smatt_study_load.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return authService.registerStudent(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}