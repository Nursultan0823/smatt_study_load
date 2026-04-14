package com.example.smatt_study_load.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.service.TeacherService;




@RestController
@RequestMapping("/teacher")
public class TeacherController {
    
    private final TeacherService teacherService;
    public TeacherController(TeacherService teacherService){
            this.teacherService=teacherService;
    }
      @GetMapping("/me")
    public ResponseEntity<?> getCurrentStudent (Authentication authentication) {
        return teacherService.getCurrentUser(authentication);
    }
      @GetMapping("/{teacherId}/disciplines")
    public List<Discipline> getTeacherDisciplines(@PathVariable int teacherId) {
        return teacherService.getDisciplinesByTeacherId(teacherId);
    } 
 


}
