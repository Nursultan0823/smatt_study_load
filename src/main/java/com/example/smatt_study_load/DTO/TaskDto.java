package com.example.smatt_study_load.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDto {
    private int id;
    private String title;
    private String description;
    private String disciplineName;
    private Integer disciplineId;
    private String teacherName;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
    private List<TaskAttachmentDto> attachments;
}
