package com.example.smatt_study_load.repository;

import com.example.smatt_study_load.enums.AnnouncementType;
import com.example.smatt_study_load.models.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    List<Announcement> findByGroupsIdOrderByCreatedAtDesc(int groupId);
    boolean existsByTypeAndTargetId(AnnouncementType type, int targetId);

}