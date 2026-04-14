package com.example.smatt_study_load.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smatt_study_load.DTO.AddScheduleDTO;
import com.example.smatt_study_load.DTO.Response;

import com.example.smatt_study_load.DTO.TeacherScheduleDto;
import com.example.smatt_study_load.DTO.UpcomingScheduleDto;
import com.example.smatt_study_load.models.Schedule;
import com.example.smatt_study_load.service.ScheduleService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/schedule")
@AllArgsConstructor
public class ScheduleController {
    
    private final ScheduleService groupLeaderService;

    @PostMapping("/add")
    public Schedule createSchedule(@RequestBody AddScheduleDTO request) {
        return groupLeaderService.createSchedule(request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable int id) {
        groupLeaderService.deleteSchedule(id);
        return ResponseEntity.ok(new Response("Расписание удалено"));
    }
    @PutMapping("/{id}")
public ResponseEntity<String> updateSchedule(@PathVariable int id,
                                             @RequestBody AddScheduleDTO dto) {
    groupLeaderService.updateSchedule(id, dto);
    return ResponseEntity.ok("Расписание обновлено");
}
  @GetMapping("/{teacherId}/schedules")
      public List<TeacherScheduleDto> getSchedulesByTeacher(@PathVariable int teacherId) {
          return groupLeaderService.getSchedulesByTeacher(teacherId);
      }
            @GetMapping("/{groupId}/student")
    public List<UpcomingScheduleDto> getUpcomingSchedules(
            @PathVariable int groupId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return groupLeaderService.getUpcomingSchedulesByGroup(groupId, limit);
    }
}
