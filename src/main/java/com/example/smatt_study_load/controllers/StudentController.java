package com.example.smatt_study_load.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smatt_study_load.service.StudentService;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("/student")
public class StudentController {
    StudentService studentService;
    public StudentController(StudentService studentService){
        this.studentService=studentService;
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentStudent (Authentication authentication) {
        return studentService.getCurrentUser(authentication);
    }

    
}
