package com.example.smatt_study_load.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetgroupDTO {
    private int id;
    private String name; // например ИВТ-21-1

    private int courseNumber;

    private String specialty;

}
