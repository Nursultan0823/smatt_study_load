package com.example.smatt_study_load.controllers;

import org.springframework.web.bind.annotation.*;

import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/pending")
    public List<User> getPendingUsers() {
        return userRepository.findByStatus(UserStatus.PENDING);
    }

    @PutMapping("/approve/{id}")
    public String approveUser(@PathVariable int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setStatus(UserStatus.APPROVED);
        user.setEnabled(true);
        userRepository.save(user);

        return "Пользователь подтвержден";
    }

    @PutMapping("/reject/{id}")
    public String rejectUser(@PathVariable int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setStatus(UserStatus.REJECTED);
        user.setEnabled(false);
        userRepository.save(user);

        return "Пользователь отклонен";
    }
}