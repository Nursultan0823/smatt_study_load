package com.example.smatt_study_load.DTO;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class UmmMaterialDto {
    private int id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int disciplineId;
    private String disciplineName;

    private int authorId;
    private String authorName;

    private List<String> urls;
    private List<UmmMaterialAttachmentDto> attachments;
}
