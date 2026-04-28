package com.example.smatt_study_load.service;

import com.example.smatt_study_load.enums.AnnouncementType;
import com.example.smatt_study_load.models.*;
import com.example.smatt_study_load.repository.AnnouncementRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeadlineReminderService {

    private final TaskRepository taskRepository;
    private final AnnouncementRepository announcementRepository;
    private final ScheduleRepository scheduleRepository;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Bishkek")
    public void createDeadlineReminders() {

        LocalDate reminderDate = LocalDate.now().plusDays(3);

        LocalDateTime start = reminderDate.atStartOfDay();
        LocalDateTime end = reminderDate.plusDays(1).atStartOfDay();

        List<Task> tasks = taskRepository.findByDeadlineBetween(start, end);

        for (Task task : tasks) {

            boolean alreadyExists = announcementRepository.existsByTypeAndTargetId(
                    AnnouncementType.TASK_DEADLINE_REMINDER,
                    task.getId()
            );

            if (alreadyExists) {
                continue;
            }

            List<GroupEntity> groups = scheduleRepository.findGroupsByDisciplineAndTeacher(
                    task.getDiscipline().getId(),
                    task.getCreatedBy().getId()
            );

            if (groups.isEmpty()) {
                continue;
            }

            Announcement announcement = new Announcement();
            announcement.setTitle("Скоро дедлайн");
            announcement.setContent(
                    "Через 3 дня дедлайн по заданию: " + task.getTitle()
            );
            announcement.setDiscipline(task.getDiscipline());
            announcement.setTeacher(task.getCreatedBy());
            announcement.setCreatedAt(LocalDateTime.now());
            announcement.setType(AnnouncementType.TASK_DEADLINE_REMINDER);
            announcement.setTargetId(task.getId());
            announcement.setGroups(groups);

            announcementRepository.save(announcement);
        }
    }
}
