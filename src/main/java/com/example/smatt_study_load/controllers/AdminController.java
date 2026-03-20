package com.example.smatt_study_load.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.UserRepository;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
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
    public ResponseEntity<?> approveUser(@PathVariable int id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Пользователь не найден");
        }

        try {
            user.setStatus(UserStatus.APPROVED);
            user.setEnabled(true);
            userRepository.save(user);

            return ResponseEntity.ok("Пользователь подтвержден");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при подтверждении пользователя");
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectUser(@PathVariable int id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Пользователь не найден");
        }

        try {
            user.setStatus(UserStatus.REJECTED);
            user.setEnabled(true);
            userRepository.save(user);

            return ResponseEntity.ok("Пользователь не подтвержден");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при подтверждении пользователя");
        }
    }
}