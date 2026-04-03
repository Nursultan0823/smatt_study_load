package com.example.smatt_study_load.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScheduleConflictCheckResponse {
    private boolean conflict;
    private boolean groupConflict;
    private boolean teacherConflict;
    private boolean roomConflict;
    private String message  ;
    
}
