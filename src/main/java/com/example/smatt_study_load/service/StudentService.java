package com.example.smatt_study_load.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.smatt_study_load.DTO.CurrentStudentDTO;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.UpcomingScheduleDto;
import com.example.smatt_study_load.models.Schedule;
import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.StudentProfileRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.UserDetailsImpl;


@Service
public class StudentService {
       private final UserRepository userRepository;
        private final StudentProfileRepository studentProfileRepository;
         private final ScheduleRepository scheduleRepository;
    public StudentService (UserRepository userRepository,StudentProfileRepository studentProfileRepository,ScheduleRepository scheduleRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository=studentProfileRepository;
        this.scheduleRepository=scheduleRepository;
    }
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
      public List<UpcomingScheduleDto> getUpcomingSchedulesByGroup(int groupId, int limit) {
        LocalDateTime now = LocalDateTime.now();

        List<Schedule> schedules = scheduleRepository.findByGroupId(groupId);

        return schedules.stream()
                .map(schedule -> {
                    LocalDateTime nextStart = getNextDateTime(schedule.getDayOfWeek(), schedule.getStartTime(), now);
                    LocalDateTime nextEnd = getNextDateTime(schedule.getDayOfWeek(), schedule.getEndTime(), now);

                    if (nextEnd.isBefore(nextStart)) {
                        nextEnd = nextStart.plusMinutes(Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes());
                    }

                    String teacherName = schedule.getTeacher() != null
                            ? schedule.getTeacher().toString()
                            : null;

                    return new UpcomingScheduleDto(
                            schedule.getId(),
                            schedule.getDiscipline().getName(),
                            teacherName,
                            schedule.getRoom(),
                            nextStart,
                            nextEnd
                    );
                })
                .sorted(Comparator.comparing(UpcomingScheduleDto::getStartDateTime))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private LocalDateTime getNextDateTime(DayOfWeek targetDay, LocalTime targetTime, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        DayOfWeek currentDay = now.getDayOfWeek();

        LocalDate targetDate;

        if (currentDay == targetDay) {
            if (targetTime.isAfter(now.toLocalTime())) {
                targetDate = today;
            } else {
                targetDate = today.with(TemporalAdjusters.next(targetDay));
            }
        } else {
            targetDate = today.with(TemporalAdjusters.nextOrSame(targetDay));
        }

        return LocalDateTime.of(targetDate, targetTime);
    }
}
