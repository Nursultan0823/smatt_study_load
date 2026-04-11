package com.example.smatt_study_load.DTO;

import java.time.DayOfWeek;
import java.time.LocalTime;

import lombok.Data;

@Data

public class TeacherScheduleDto {
        private int id;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private String url;
    private String disciplineName;
    private String groupName;
}
