package com.example.smatt_study_load.service;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.smatt_study_load.DTO.CurrentStudentDTO;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.User;

import com.example.smatt_study_load.repository.StudentProfileRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.UserDetailsImpl;

import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
public class StudentService {
       private final UserRepository userRepository;
        private final StudentProfileRepository studentProfileRepository;
    
      public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body(new Response("Пользователь не авторизован"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        StudentProfile studentProfile=studentProfileRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        CurrentStudentDTO dto = new CurrentStudentDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList(),
                studentProfile.getGroup().toString(),
                studentProfile.getId()
        );

        return ResponseEntity.ok(dto);
    }
      

   
}
