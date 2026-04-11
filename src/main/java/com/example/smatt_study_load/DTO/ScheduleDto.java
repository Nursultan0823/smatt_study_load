package com.example.smatt_study_load.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScheduleDto {
    private int id;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String room;
    private String disciplineName;
    private String teacherName;
    private String position;
    private String url;
}
