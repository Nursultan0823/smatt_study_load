package com.example.smatt_study_load.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetDisciplineDTO {
    private int id;
    private String name;
    private String description;
}
