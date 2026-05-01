package com.example.smatt_study_load.DTO;

import com.example.smatt_study_load.enums.AnnouncementType;
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

    private AnnouncementType type;
    private int targetId;

    private String meetingUrl;

    private boolean seen;
}
