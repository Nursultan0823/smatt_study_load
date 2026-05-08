package com.example.smatt_study_load.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeacherDirectoryItemDto {
    private int id;
    private String fullName;
    private String email;
    private String position;
    private int disciplinesCount;
    private int materialsCount;
}
