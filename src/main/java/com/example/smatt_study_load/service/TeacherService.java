package com.example.smatt_study_load.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.smatt_study_load.DTO.CurrentTeacherDTO;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.TeacherScheduleDto;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.Schedule;
import com.example.smatt_study_load.models.Task;
import com.example.smatt_study_load.models.TaskAttachment;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.TaskRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.UserDetailsImpl;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TeacherService {
         private final UserRepository userRepository;
        private final TeacherProfileRepository teacherProfileRepository;
        private final ScheduleRepository scheduleRepository;
        private final DisciplineRepository disciplineRepository;
        private final TaskRepository taskRepository;
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
    public List<TeacherScheduleDto> getSchedulesByTeacher(int teacherId) {
    List<Schedule> schedules = scheduleRepository.findByTeacherIdOrderByDayOfWeekAscStartTimeAsc(teacherId);

    return schedules.stream().map(schedule -> {
        TeacherScheduleDto dto = new TeacherScheduleDto();
        dto.setId(schedule.getId());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setRoom(schedule.getRoom());
        dto.setUrl(schedule.getUrl());
        dto.setDisciplineName(schedule.getDiscipline().getName());
        dto.setGroupName(schedule.getGroup().getName());
        return dto;
    }).toList();
}
public void addTaskWithFile(String title,
                            String description,
                            int disciplineId,
                            int createdById,
                            MultipartFile file) {
    Discipline discipline = disciplineRepository.findById(disciplineId)
            .orElseThrow(() -> new RuntimeException("Дисциплина не найдена"));

    TeacherProfile teacher = teacherProfileRepository.findById(createdById)
            .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

    Task task = new Task();
    task.setTitle(title);
    task.setDescription(description);
    task.setDiscipline(discipline);
    task.setCreatedBy(teacher);

    if (file != null && !file.isEmpty()) {
        try {
            TaskAttachment attachment = new TaskAttachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setContentType(file.getContentType());
            attachment.setFileData(file.getBytes());
            attachment.setTask(task);

            task.getAttachments().add(attachment);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении файла");
        }
    }

    taskRepository.save(task);
}
}
