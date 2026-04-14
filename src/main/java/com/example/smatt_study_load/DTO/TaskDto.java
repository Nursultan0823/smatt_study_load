package com.example.smatt_study_load.DTO;

import lombok.Data;

import java.util.List;

@Data
public class TaskDto {
    private int id;
    private String title;
    private String description;
    private String disciplineName;
    private String teacherName;
    private List<TaskAttachmentDto> attachments;
}
