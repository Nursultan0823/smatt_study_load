package com.example.smatt_study_load.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smatt_study_load.DTO.TeacherDetailDto;
import com.example.smatt_study_load.DTO.TeacherDirectoryItemDto;
import com.example.smatt_study_load.service.TeacherService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/teachers")
@AllArgsConstructor
public class TeacherDirectoryController {

    private final TeacherService teacherService;

    @GetMapping
    public List<TeacherDirectoryItemDto> getTeachers() {
        return teacherService.getTeacherDirectory();
    }

    @GetMapping("/{teacherId}")
    public TeacherDetailDto getTeacher(@PathVariable int teacherId) {
        return teacherService.getTeacherDetail(teacherId);
    }

    @GetMapping("/{teacherId}/avatar")
    public ResponseEntity<byte[]> getTeacherAvatar(@PathVariable int teacherId) {
        return teacherService.getTeacherAvatar(teacherId);
    }
}
