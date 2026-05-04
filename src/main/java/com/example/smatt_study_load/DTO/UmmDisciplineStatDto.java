package com.example.smatt_study_load.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UmmDisciplineStatDto {
    private int disciplineId;
    private String disciplineName;
    private long materialCount;
}
