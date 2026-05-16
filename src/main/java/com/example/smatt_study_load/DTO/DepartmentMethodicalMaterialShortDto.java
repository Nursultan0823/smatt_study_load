package com.example.smatt_study_load.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DepartmentMethodicalMaterialShortDto {
    private int id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer disciplineId;
    private String disciplineName;

    private int uploadedById;
    private String uploadedByName;

    private int attachmentsCount;
    private int urlsCount;
}
