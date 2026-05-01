package com.example.smatt_study_load.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smatt_study_load.DTO.AnnouncementDto;
import com.example.smatt_study_load.service.AnnouncementServuce;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class AnnouncementController {
    AnnouncementServuce announcementServuce;

    @GetMapping("/group/{groupId}/user/{userId}")
public ResponseEntity<List<AnnouncementDto>> getAnnouncementsForGroup(
        @PathVariable int groupId,
        @PathVariable int userId
) {
    return ResponseEntity.ok(
            announcementServuce.getAnnouncementsForGroup(groupId, userId)
    );
}

@GetMapping("/teacher/{teacherId}/user/{userId}/lesson-reminders")
public ResponseEntity<List<AnnouncementDto>> getLessonRemindersForTeacher(
        @PathVariable int teacherId,
        @PathVariable int userId
) {
    return ResponseEntity.ok(
            announcementServuce.getLessonRemindersForTeacher(teacherId, userId)
    );
}

@GetMapping("/group/{groupId}/user/{userId}/history")
public ResponseEntity<List<AnnouncementDto>> getAnnouncementHistoryForGroup(
        @PathVariable int groupId,
        @PathVariable int userId
) {
    return ResponseEntity.ok(
            announcementServuce.getAnnouncementHistoryForGroup(groupId, userId)
    );
}

@GetMapping("/teacher/{teacherId}/user/{userId}/lesson-reminders/history")
public ResponseEntity<List<AnnouncementDto>> getLessonReminderHistoryForTeacher(
        @PathVariable int teacherId,
        @PathVariable int userId
) {
    return ResponseEntity.ok(
            announcementServuce.getLessonReminderHistoryForTeacher(teacherId, userId)
    );
}

@PostMapping("/{announcementId}/seen/user/{userId}")
public ResponseEntity<?> markAsSeen(
        @PathVariable int announcementId,
        @PathVariable int userId
) {
    announcementServuce.markAnnouncementAsSeen(announcementId, userId);
    return ResponseEntity.ok("Объявление отмечено как просмотренное");
}
}
