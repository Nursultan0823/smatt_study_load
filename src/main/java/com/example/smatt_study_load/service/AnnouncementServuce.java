package com.example.smatt_study_load.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smatt_study_load.DTO.AnnouncementDto;
import com.example.smatt_study_load.enums.AnnouncementType;
import com.example.smatt_study_load.models.Announcement;
import com.example.smatt_study_load.models.AnnouncementReadStatus;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.AnnouncementReadStatusRepository;
import com.example.smatt_study_load.repository.AnnouncementRepository;
import com.example.smatt_study_load.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AnnouncementServuce {
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementReadStatusRepository announcementReadStatusRepository;
    private final UserRepository userRepository;
    public List<AnnouncementDto> getAnnouncementsForGroup(int groupId, int userId) {
    return announcementRepository.findByGroupsIdOrderByCreatedAtDesc(groupId)
            .stream()
            .map(announcement -> toDto(announcement, userId))
            .filter(announcement -> !announcement.isSeen())
            .toList();
}

public List<AnnouncementDto> getLessonRemindersForTeacher(int teacherId, int userId) {
    return announcementRepository
            .findByTeacherIdAndTypeOrderByCreatedAtDesc(
                    teacherId,
                    AnnouncementType.LESSON_STARTING_SOON
            )
            .stream()
            .map(announcement -> toDto(announcement, userId))
            .filter(announcement -> !announcement.isSeen())
            .toList();
}

public List<AnnouncementDto> getAnnouncementHistoryForGroup(int groupId, int userId) {
    return announcementRepository.findByGroupsIdOrderByCreatedAtDesc(groupId)
            .stream()
            .map(announcement -> toDto(announcement, userId))
            .toList();
}

public List<AnnouncementDto> getLessonReminderHistoryForTeacher(int teacherId, int userId) {
    return announcementRepository
            .findByTeacherIdAndTypeOrderByCreatedAtDesc(
                    teacherId,
                    AnnouncementType.LESSON_STARTING_SOON
            )
            .stream()
            .map(announcement -> toDto(announcement, userId))
            .toList();
}

private AnnouncementDto toDto(Announcement announcement, int userId) {
    boolean seen = announcementReadStatusRepository
            .existsByAnnouncementIdAndUserIdAndSeenTrue(
                    announcement.getId(),
                    userId
            );

    return new AnnouncementDto(
            announcement.getId(),
            announcement.getTitle(),
            announcement.getContent(),
            announcement.getCreatedAt(),
            announcement.getDiscipline().getName(),
            announcement.getTeacher().getUser().getFullName(),
            announcement.getType(),
            announcement.getTargetId(),
            announcement.getMeetingUrl(),
            seen
    );
}
@Transactional
public void markAnnouncementAsSeen(int announcementId, int userId) {
    AnnouncementReadStatus status = announcementReadStatusRepository
            .findByAnnouncementIdAndUserId(announcementId, userId)
            .orElseGet(() -> {
                Announcement announcement = announcementRepository.findById(announcementId)
                        .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

                AnnouncementReadStatus newStatus = new AnnouncementReadStatus();
                newStatus.setAnnouncement(announcement);
                newStatus.setUser(user);
                return newStatus;
            });

    status.setSeen(true);
    status.setSeenAt(LocalDateTime.now());

    announcementReadStatusRepository.save(status);
}
}
