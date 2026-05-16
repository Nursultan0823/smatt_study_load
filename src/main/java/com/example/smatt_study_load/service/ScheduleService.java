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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.smatt_study_load.DTO.AddScheduleDTO;
import com.example.smatt_study_load.DTO.GetDisciplineDTO;
import com.example.smatt_study_load.DTO.ScheduleDto;
import com.example.smatt_study_load.DTO.TeacherScheduleDto;
import com.example.smatt_study_load.DTO.TeacherShortDto;
import com.example.smatt_study_load.DTO.UpcomingScheduleDto;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.GroupEntity;
import com.example.smatt_study_load.models.Schedule;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.GroupEntityRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final GroupEntityRepository groupRepository;
    private final DisciplineRepository disciplineRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    public Schedule createSchedule(AddScheduleDTO dto) {


        GroupEntity group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));

        Discipline discipline = disciplineRepository.findById(dto.getDisciplineId())
                .orElseThrow(() -> new RuntimeException("Дисциплина не найдена"));

        TeacherProfile teacher = teacherProfileRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        Schedule schedule = new Schedule();
        schedule.setDayOfWeek(dto.getDayOfWeek());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setRoom(dto.getRoom());
        schedule.setUrl(dto.getUrl());
        schedule.setGroup(group);
        schedule.setDiscipline(discipline);
        schedule.setTeacher(teacher);

        return scheduleRepository.save(schedule);
    }
    public void deleteSchedule(int id) {
    if (!scheduleRepository.existsById(id)) {
        throw new RuntimeException("Расписание не найдено");
    }
    scheduleRepository.deleteById(id);
}
public void updateSchedule(int id, AddScheduleDTO dto) {
    Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Расписание не найдено"));

    GroupEntity group = groupRepository.findById(dto.getGroupId())
            .orElseThrow(() -> new RuntimeException("Группа не найдена"));

    Discipline discipline = disciplineRepository.findById(dto.getDisciplineId())
            .orElseThrow(() -> new RuntimeException("Дисциплина не найдена"));

    TeacherProfile teacher = teacherProfileRepository.findById(dto.getTeacherId())
            .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

    schedule.setDayOfWeek(dto.getDayOfWeek());
    schedule.setStartTime(dto.getStartTime());
    schedule.setEndTime(dto.getEndTime());
    schedule.setRoom(dto.getRoom());
    schedule.setUrl(dto.getUrl());
    schedule.setGroup(group);
    schedule.setDiscipline(discipline);
    schedule.setTeacher(teacher);

    scheduleRepository.save(schedule);
}
  public List<TeacherScheduleDto> getSchedulesByTeacher(int teacherId) {
    teacherProfileRepository.findByIdAndUser_EnabledTrueAndUser_Status(teacherId, UserStatus.APPROVED)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Преподаватель не найден"));

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

                    String teacherName = getTeacherName(schedule.getTeacher());

                    return new UpcomingScheduleDto(
                            schedule.getId(),
                            schedule.getDiscipline().getName(),
                            teacherName,
                            schedule.getRoom(),
                            schedule.getUrl(),
                            nextStart,
                            nextEnd
                    );
                })
                .sorted(Comparator.comparing(UpcomingScheduleDto::getStartDateTime))
                .limit(limit)
                .collect(Collectors.toList());
    }
    public List<ScheduleDto> getSchedulesByGroup(int groupId) {
        List<Schedule> schedules = scheduleRepository.findByGroupIdOrderByDayOfWeekAscStartTimeAsc(groupId);

        return schedules.stream()
                .map(schedule -> new ScheduleDto(
                        schedule.getId(),
                        schedule.getDayOfWeek().name(),
                        schedule.getStartTime().toString(),
                        schedule.getEndTime().toString(),
                        schedule.getRoom(),
                        schedule.getDiscipline().getName(),
                        getTeacherName(schedule.getTeacher()),
                        schedule.getTeacher() != null ? schedule.getTeacher().getPosition() : null,
                        schedule.getUrl()
                ))
                .toList();
    }

    public List<GetDisciplineDTO> getAllDisciplines() {
        return disciplineRepository.findAllDisciplineDTO();
    }

    public List<TeacherShortDto> getAllTeachers() {
        return teacherProfileRepository
                .findByUser_EnabledTrueAndUser_StatusOrderByUser_FullNameAsc(UserStatus.APPROVED)
                .stream()
                .map(tp -> new TeacherShortDto(
                        tp.getId(),
                        tp.getUser() != null ? tp.getUser().getFullName() : "Неизвестно",
                        tp.getPosition()
                ))
                .toList();
    }

    private String getTeacherName(TeacherProfile teacher) {
        if (teacher == null || teacher.getUser() == null) {
            return "Неизвестно";
        }
        return teacher.getUser().getFullName();
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
