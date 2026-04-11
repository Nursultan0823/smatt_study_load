package com.example.smatt_study_load.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smatt_study_load.DTO.AddScheduleDTO;
import com.example.smatt_study_load.DTO.Response;
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
    @DeleteMapping("/schedule/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable int id) {
        groupLeaderService.deleteSchedule(id);
        return ResponseEntity.ok(new Response("Расписание удалено"));
    }
    @PutMapping("/schedule/{id}")
public ResponseEntity<String> updateSchedule(@PathVariable int id,
                                             @RequestBody AddScheduleDTO dto) {
    groupLeaderService.updateSchedule(id, dto);
    return ResponseEntity.ok("Расписание обновлено");
}
}
