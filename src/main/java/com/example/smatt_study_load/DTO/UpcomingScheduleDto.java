package com.example.smatt_study_load.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UpcomingScheduleDto {
    private int scheduleId;
    private String disciplineName;
    private String teacherName;
    private String room;
    private String url;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
