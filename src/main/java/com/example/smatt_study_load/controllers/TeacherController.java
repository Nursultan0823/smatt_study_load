package com.example.smatt_study_load.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smatt_study_load.DTO.GrageDTO;
import com.example.smatt_study_load.DTO.ReportDTO;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.service.StudentService;
import com.example.smatt_study_load.service.TeacherService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;






@RestController
@RequestMapping("/teacher")
@AllArgsConstructor
public class TeacherController {
    
    private final TeacherService teacherService;
    private final StudentService studentService;
      @GetMapping("/me")
    public ResponseEntity<?> getCurrentStudent (Authentication authentication) {
        return teacherService.getCurrentUser(authentication);
    }
      @GetMapping("/{teacherId}/disciplines")
    public List<Discipline> getTeacherDisciplines(@PathVariable int teacherId) {
        return teacherService.getDisciplinesByTeacherId(teacherId);
    } 
 
@GetMapping("/task/{taskId}/reports")
public List<ReportDTO> getReportsByTaskId(@PathVariable int taskId) {
    return studentService.getReportsByTaskId(taskId);
}
@GetMapping("/repost/attachment/{attachmentId}/download")
public ResponseEntity<byte[]> downloadAttachment(@PathVariable int attachmentId) {
    return studentService.downloadAttachment(attachmentId);
}
    @PostMapping("/report/grade")
    public ResponseEntity<?> postMethodName(@RequestBody GrageDTO grageDTO) {
        try{
            studentService.updateGrate(grageDTO);
            return ResponseEntity.ok(new Response("Оценка добавлена"));
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("Ошибка оценки"));
        }
    }
    
}
