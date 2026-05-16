package com.example.smatt_study_load.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.smatt_study_load.DTO.DisciplineDto;
import com.example.smatt_study_load.DTO.ReportDTO;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.service.StudentService;
import com.example.smatt_study_load.service.TeacherService;

import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;





@RestController
@RequestMapping("/student")
@AllArgsConstructor
public class StudentController {
   private final StudentService studentService;
    private final TeacherService teacherService;
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentStudent (Authentication authentication) {
        return studentService.getCurrentUser(authentication);
    }

    @PostMapping("/report")
    public ResponseEntity<?> addReport(     @RequestParam String title,
            @RequestParam String comment,
            @RequestParam int studentId,
            @RequestParam int taskId,
            @RequestParam(required = false)List< MultipartFile> file  ) {
    
        try{
            studentService.addReport(comment, studentId, taskId, file);
            return ResponseEntity.ok(new Response("отчет успешно добавлен"));
        }
        catch(ResponseStatusException e){
                return ResponseEntity.status(e.getStatusCode()).body(new Response(e.getReason()));
        }
        catch(Exception e){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("Ошибка добавления отчета"));
        }
       
    }
    @GetMapping("/task/{taskId}/student/{studentId}")
public List<ReportDTO> getReportsByTaskIdAndStudentId(@PathVariable int taskId,
                                                      @PathVariable int studentId) {
    return studentService.getReportsByTaskIdAndStudentId(taskId, studentId);
}
    @GetMapping("/{studentId}/reports")
public List<ReportDTO> getAllReportsByStudent(@PathVariable int studentId) {
    return studentService.getAllReportsByStudent(studentId);
}
@GetMapping("/repost/attachment/{attachmentId}/download")
public ResponseEntity<byte[]> downloadAttachment(@PathVariable int attachmentId) {
    return studentService.downloadAttachment(attachmentId);
}
    @DeleteMapping("/report/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable int reportId){
        try {
            studentService.deleteReport(reportId);
            return ResponseEntity.ok(new Response( "Отчет успешно удален"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new Response(e.getReason()));
        }
    }
      @DeleteMapping("/report/attachment/{attachmentId}")
    public ResponseEntity<?> deleteReportAttachment(@PathVariable int attachmentId){
        try {
            studentService.deleteReportAttachment(attachmentId);
            return ResponseEntity.ok(new Response( "Файл успешно удален"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new Response(e.getReason()));
        }
    }
    @GetMapping("/umm/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAUmm(@PathVariable int attachmentId) {
        return teacherService.downloadAttachment(attachmentId);
    }
    @GetMapping("/group/{groupId}/disciplines")
public List<DisciplineDto> getDisciplinesByGroupId(@PathVariable int groupId) {
    return studentService.getDisciplinesByGroupId(groupId);
}
}
