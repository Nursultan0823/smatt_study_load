package com.example.smatt_study_load.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeacherShortDto {
    private int id;
    private String fullName;
    private String position;
}
