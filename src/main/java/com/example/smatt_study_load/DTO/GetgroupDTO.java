package com.example.smatt_study_load.DTO;

import lombok.Data;

@Data
public class GetgroupDTO {
    private int id;
    private String name; // например ИВТ-21-1

    private Integer courseNumber;

    private String specialty;
}
