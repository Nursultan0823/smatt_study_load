package com.example.smatt_study_load.DTO;

import lombok.Data;

import java.util.List;

@Data
public class DisciplineDto {
    private int id;
    private String name;
    private String description;
    private List<String> urlList;
    private List<UmmDTO>ummfiles;   
}
