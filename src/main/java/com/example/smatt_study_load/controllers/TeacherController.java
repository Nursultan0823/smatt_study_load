package com.example.smatt_study_load.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.smatt_study_load.DTO.DisciplineDto;
import com.example.smatt_study_load.DTO.GrageDTO;
import com.example.smatt_study_load.DTO.ReportDTO;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.models.ReportAttachment;
import com.example.smatt_study_load.models.UmmFile;
import com.example.smatt_study_load.repository.ReporAttachmentRepository;
import com.example.smatt_study_load.repository.UmmFileRepository;
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
    private final ReporAttachmentRepository reporAttachmentRepository;
    private final UmmFileRepository ummFileRepository;
      @GetMapping("/me")
    public ResponseEntity<?> getCurrentStudent (Authentication authentication) {
        return teacherService.getCurrentUser(authentication);
    }
      @GetMapping("/{teacherId}/disciplines")
    public List<DisciplineDto> getTeacherDisciplines(@PathVariable int teacherId) {
        return teacherService.getDisciplinesTeacherId(teacherId);
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
    @PostMapping("/grade/checked")
    public ResponseEntity<?> checkedGrade(@RequestParam int reportId,
                                        @RequestParam String comment,
                                        @RequestParam(required = false) List<MultipartFile> files )
     {
            try{
            studentService.checkedGrade(reportId,comment,files);
            return ResponseEntity.ok(new Response("Замечания добавлена"));
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("Ошибка замечания"));
        }
    }
          @DeleteMapping("/report/attachment/{attachmentId}")
    public ResponseEntity<?> deleteReportAttachment(@PathVariable int attachmentId){
        ReportAttachment report = reporAttachmentRepository.findById(attachmentId).orElseThrow(() -> new RuntimeException("Файл не найден"));
        reporAttachmentRepository.delete(report);
        return ResponseEntity.ok(new Response( "Файл успешно удален"));
    }
    @PostMapping("/umm/add")
    public ResponseEntity<?> AddUmm(@RequestParam int disciplineId,
                                    @RequestParam(required = false) List<String>urls,
                                    @RequestParam(required = false) List<MultipartFile> files) 
    {
           try{
            return ResponseEntity.ok(new Response("UMM добавлена"));
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("Ошибка UMM"));
        }
    }
    @DeleteMapping("/{disciplineId}/url")
    public ResponseEntity<?> removeUrl(@PathVariable int disciplineId,
                                   @RequestParam String url) {
    teacherService.removeUrlFromDiscipline(disciplineId, url);
    return ResponseEntity.ok("URL удален");
    }
    @DeleteMapping("/umm/{ummId}")
    public ResponseEntity<?> removeUmm(@PathVariable int ummId) {
        UmmFile ummFile = ummFileRepository.findById(ummId).orElseThrow(() -> new RuntimeException("Файл не найден"));
        ummFileRepository.delete(ummFile);
        return ResponseEntity.ok("UMM удален");
    }
    @GetMapping("/umm/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAUmm(@PathVariable int attachmentId) {
        return teacherService.downloadAttachment(attachmentId);
    }
}
