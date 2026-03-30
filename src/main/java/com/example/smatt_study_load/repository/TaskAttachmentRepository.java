package com.example.smatt_study_load.repository;

import com.example.smatt_study_load.models.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Integer> {
    List<TaskAttachment> findByTaskId(int taskId);
}
