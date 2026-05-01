package com.example.smatt_study_load.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.smatt_study_load.enums.AnnouncementType;
import com.example.smatt_study_load.models.Announcement;
import com.example.smatt_study_load.models.Schedule;
import com.example.smatt_study_load.repository.AnnouncementRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LessonReminderService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Bishkek");

    // Окно срабатывания напоминания: за 9-11 минут до начала.
    // Cron запускается каждую минуту, поэтому окно даёт небольшой запас на случай
    // пропуска тика и одновременно не приводит к дублированию благодаря дедупу ниже.
    private static final long REMIND_MIN_MINUTES = 9;
    private static final long REMIND_MAX_MINUTES = 11;

    private final ScheduleRepository scheduleRepository;
    private final AnnouncementRepository announcementRepository;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Bishkek")
    public void createLessonReminders() {
        LocalDateTime now = LocalDateTime.now(ZONE);
        DayOfWeek today = now.getDayOfWeek();

        List<Schedule> schedules = scheduleRepository.findOnlineByDayOfWeek(today);

        if (schedules.isEmpty()) {
            return;
        }

        LocalDate todayDate = now.toLocalDate();

        for (Schedule schedule : schedules) {
            if (schedule.getStartTime() == null || schedule.getUrl() == null
                    || schedule.getUrl().isBlank()) {
                continue;
            }

            LocalDateTime lessonStart = LocalDateTime.of(todayDate, schedule.getStartTime());
            long minutesUntilStart = Duration.between(now, lessonStart).toMinutes();

            if (minutesUntilStart < REMIND_MIN_MINUTES || minutesUntilStart > REMIND_MAX_MINUTES) {
                continue;
            }

            // Дедуп в пределах одного занятия: если за последние 2 часа уже создавали
            // уведомление по этому schedule.id — пропускаем.
            boolean alreadyExists = announcementRepository.existsByTypeAndTargetIdAndCreatedAtAfter(
                    AnnouncementType.LESSON_STARTING_SOON,
                    schedule.getId(),
                    now.minusHours(2)
            );

            if (alreadyExists) {
                continue;
            }

            String disciplineName = schedule.getDiscipline() != null
                    ? schedule.getDiscipline().getName()
                    : "занятие";

            String content = String.format(
                    "Через 10 минут начнётся онлайн занятие по дисциплине \"%s\". Подключайтесь по ссылке: %s",
                    disciplineName,
                    schedule.getUrl()
            );

            Announcement announcement = new Announcement();
            announcement.setTitle("Скоро онлайн занятие");
            announcement.setContent(content);
            announcement.setDiscipline(schedule.getDiscipline());
            announcement.setTeacher(schedule.getTeacher());
            announcement.setCreatedAt(now);
            announcement.setType(AnnouncementType.LESSON_STARTING_SOON);
            announcement.setTargetId(schedule.getId());
            announcement.setMeetingUrl(schedule.getUrl());

            if (schedule.getGroup() != null) {
                announcement.setGroups(List.of(schedule.getGroup()));
            }

            announcementRepository.save(announcement);
        }
    }
}
