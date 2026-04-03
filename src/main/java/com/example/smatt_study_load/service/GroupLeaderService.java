package com.example.smatt_study_load.service;

import org.springframework.stereotype.Service;

import com.example.smatt_study_load.DTO.AddScheduleDTO;
import com.example.smatt_study_load.DTO.ScheduleConflictCheckResponse;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.GroupEntity;
import com.example.smatt_study_load.models.Schedule;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.GroupEntityRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GroupLeaderService {
    private final ScheduleRepository scheduleRepository;
    private final GroupEntityRepository groupRepository;
    private final DisciplineRepository disciplineRepository;
    private final TeacherProfileRepository teacherProfileRepository;

   public ScheduleConflictCheckResponse validateSchedule(
            int groupId,
            int teacherId,
            String room,
            java.time.DayOfWeek dayOfWeek,
            java.time.LocalTime startTime,
            java.time.LocalTime endTime
    ) {
        if (dayOfWeek == null || startTime == null || endTime == null) {
            return new ScheduleConflictCheckResponse(
                    true, false, false, false,
                    "День недели и время обязательны"
            );
        }

        if (!startTime.isBefore(endTime)) {
            return new ScheduleConflictCheckResponse(
                    true, false, false, false,
                    "Время начала должно быть меньше времени окончания"
            );
        }

        boolean groupConflict = false;
        boolean teacherConflict = false;
        boolean roomConflict = false;

        if (groupId > 0) {
            groupConflict = scheduleRepository.existsGroupConflict(
                    groupId, dayOfWeek, startTime, endTime
            );
        }

        if (teacherId > 0) {
            teacherConflict = scheduleRepository.existsTeacherConflict(
                    teacherId, dayOfWeek, startTime, endTime
            );
        }

        if (room != null && !room.isBlank()) {
            roomConflict = scheduleRepository.existsRoomConflict(
                    room, dayOfWeek, startTime, endTime
            );
        }

        boolean conflict = groupConflict || teacherConflict || roomConflict;

        String message = "Конфликтов нет";
        if (groupConflict) {
            message = "У группы уже есть занятие в это время";
        } else if (teacherConflict) {
            message = "У преподавателя уже есть занятие в это время";
        } else if (roomConflict) {
            message = "Аудитория уже занята в это время";
        }

        return new ScheduleConflictCheckResponse(
                conflict,
                groupConflict,
                teacherConflict,
                roomConflict,
                message
        );
    }

    @Transactional
    public Schedule createSchedule(AddScheduleDTO dto) {
        ScheduleConflictCheckResponse validation = validateSchedule(
                dto.getGroupId(),
                dto.getTeacherId(),
                dto.getRoom(),
                dto.getDayOfWeek(),
                dto.getStartTime(),
                dto.getEndTime()
        );

        if (validation.isConflict()) {
            throw new RuntimeException(validation.getMessage());
        }

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
}
