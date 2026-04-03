package com.example.smatt_study_load.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.smatt_study_load.DTO.CurrentTeacherDTO;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.UserDetailsImpl;

@Service
public class TeacherService {
         private final UserRepository userRepository;
        private final TeacherProfileRepository teacherProfileRepository;
        private final ScheduleRepository scheduleRepository;
    public TeacherService (UserRepository userRepository,
                            TeacherProfileRepository teacherProfileRepository,
                            ScheduleRepository scheduleRepository) {
        this.userRepository = userRepository;
        this.teacherProfileRepository=teacherProfileRepository;
        this.scheduleRepository=scheduleRepository;
    }
      public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body(new Response("Пользователь не авторизован"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        TeacherProfile teacherProfile=teacherProfileRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        CurrentTeacherDTO dto = new CurrentTeacherDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList(),
                teacherProfile.getPosition().toString(),
                teacherProfile.getId()
        );

        return ResponseEntity.ok(dto);
    }
     public List<Discipline> getDisciplinesByTeacherId(int teacherId) {
        return scheduleRepository.findDisciplinesByTeacherId(teacherId);
    }
}
