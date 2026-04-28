package com.example.smatt_study_load.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smatt_study_load.models.AnnouncementReadStatus;

public interface AnnouncementReadStatusRepository extends JpaRepository<AnnouncementReadStatus,Integer> {
        Optional<AnnouncementReadStatus> findByAnnouncementIdAndUserId(int announcementId, int userId);

    boolean existsByAnnouncementIdAndUserIdAndSeenTrue(int announcementId, int userId);
}
