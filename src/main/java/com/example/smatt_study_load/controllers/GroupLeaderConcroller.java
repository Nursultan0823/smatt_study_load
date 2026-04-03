package com.example.smatt_study_load.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smatt_study_load.DTO.AddScheduleDTO;
import com.example.smatt_study_load.DTO.ScheduleConflictCheckResponse;
import com.example.smatt_study_load.models.Schedule;
import com.example.smatt_study_load.service.GroupLeaderService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/leader")
@AllArgsConstructor
public class GroupLeaderConcroller {
    
    private final GroupLeaderService groupLeaderService;

     @PostMapping("/check-conflict")
    public ScheduleConflictCheckResponse checkConflict(@RequestBody AddScheduleDTO request) {
        return groupLeaderService.validateSchedule(
                request.getGroupId(),
                request.getTeacherId(),
                request.getRoom(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime()
        );
    }

    @PostMapping
    public Schedule createSchedule(@RequestBody AddScheduleDTO request) {
        return groupLeaderService.createSchedule(request);
    }
}
