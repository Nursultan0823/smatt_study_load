package com.example.smatt_study_load.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UmmMaterialShortDto {
    private int id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int disciplineId;
    private String disciplineName;

    private int authorId;
    private String authorName;

    private String materialKind;
    private String section;

    private int attachmentsCount;
    private int urlsCount;
}
