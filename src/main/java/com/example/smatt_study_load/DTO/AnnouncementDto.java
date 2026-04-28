package com.example.smatt_study_load.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AnnouncementDto {

    private int id;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    private String disciplineName;
    private String teacherName;

    private boolean seen;
}
