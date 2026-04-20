package com.example.smatt_study_load.service;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.smatt_study_load.DTO.CurrentStudentDTO;
import com.example.smatt_study_load.DTO.DisciplineDto;
import com.example.smatt_study_load.DTO.GrageDTO;
import com.example.smatt_study_load.DTO.ReportAttachmentDto;
import com.example.smatt_study_load.DTO.ReportDTO;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.UmmDTO;
import com.example.smatt_study_load.enums.ReportStatus;
import com.example.smatt_study_load.models.Report;
import com.example.smatt_study_load.models.ReportAttachment;
import com.example.smatt_study_load.models.Schedule;
import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.Task;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.ReporAttachmentRepository;
import com.example.smatt_study_load.repository.ReportRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.StudentProfileRepository;
import com.example.smatt_study_load.repository.TaskRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.UserDetailsImpl;

import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
public class StudentService {
       private final UserRepository userRepository;
        private final StudentProfileRepository studentProfileRepository;
        private final ReportRepository reportRepository;
        private final TaskRepository taskRepository;
        private final ReporAttachmentRepository reporAttachmentRepository;
        private final TeacherProfileRepository teacherProfileRepository;
        private final ScheduleRepository scheduleRepository;
      public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body(new Response("Пользователь не авторизован"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        StudentProfile studentProfile=studentProfileRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        CurrentStudentDTO dto = new CurrentStudentDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList(),
                studentProfile.getGroup().getName(),
                studentProfile.getId(),
                studentProfile.getGroup().getId()
        );

        return ResponseEntity.ok(dto);
    }
      public void addReport(String comment,
                        int studentId,
                        int taskId,
                       List<MultipartFile> files){
        StudentProfile studentProfile = studentProfileRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Студент не найден"));
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Задача не найдена"));
            Report report=new Report();
            report.setComment(comment);
            report.setStatus(ReportStatus.SUBMITTED);
            report.setStudent(studentProfile);
            report.setSubmittedAt(LocalDateTime.now());
            report.setTask(task);
             if (files != null && !files.isEmpty()) { 
            for (MultipartFile file : files) { 
            if (file != null && !file.isEmpty()) {
        try {
            ReportAttachment reportAttachment=new ReportAttachment();
            reportAttachment.setFileName(file.getOriginalFilename());
            reportAttachment.setContentType(file.getContentType());
            reportAttachment.setFileData(file.getBytes());
            reportAttachment.setReport(report);
                report.getAttachments().add(reportAttachment);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении файла");
        }
    }
}
    }
    reportRepository.save(report);
}
public void updateReport(int id,
                        String comment,
                        Integer studentId,
                        Integer taskId,
                        List<MultipartFile> files){
        Report report =reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Отчет не найден"));

        if(studentId != null){
        StudentProfile studentProfile = studentProfileRepository.findById(studentId)
        .orElseThrow(() -> new RuntimeException("Студент не найден"));
        report.setStudent(studentProfile);
        }
        if(taskId != null){
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Задача не найдена"));
            report.setTask(task);
        }
        if(comment !=null){
            report.setComment(comment);
        }
         if (files != null && !files.isEmpty()) { 
            for (MultipartFile file : files) {  
            if (file != null && !file.isEmpty()) {
        try {
            ReportAttachment reportAttachment=new ReportAttachment();
            reportAttachment.setFileName(file.getOriginalFilename());
            reportAttachment.setContentType(file.getContentType());
            reportAttachment.setFileData(file.getBytes());
            reportAttachment.setReport(report);
                report.getAttachments().add(reportAttachment);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении файла");
        }
    }
    }
}
    reportRepository.save(report);
}
@Transactional(readOnly = true)
public List<ReportDTO> getReportsByTaskId(int taskId) {
    return reportRepository.findByTaskId(taskId).stream()
            .map(report -> {
                ReportDTO dto = new ReportDTO();
                dto.setId(report.getId());
                dto.setComment(report.getComment());
                dto.setGrade(report.getGrade());
                dto.setStatus(report.getStatus());
                dto.setSubmittedAt(report.getSubmittedAt());
                dto.setTaskId(report.getTask().getId());
                dto.setTaskTitle(report.getTask().getTitle());
                dto.setStudentId(report.getStudent().getId());
                dto.setStudentName(report.getStudent().getUser().getFullName());

                if (report.getSubmittedByUser() != null) {
                    dto.setSubmittedByUserName(report.getSubmittedByUser().getFullName());
                }

                dto.setAttachments(
                        report.getAttachments().stream()
                                .map(att -> {
                                    ReportAttachmentDto attachmentDto = new ReportAttachmentDto();
                                    attachmentDto.setId(att.getId());
                                    attachmentDto.setFileName(att.getFileName());
                                    attachmentDto.setContentType(att.getContentType());
                                    return attachmentDto;
                                })
                                .toList()
                );

                return dto;
            })
            .toList();
}
   @Transactional(readOnly = true)
public List<ReportDTO> getReportsByTaskIdAndStudentId(int taskId, int studentId) {
    return reportRepository.findByTaskIdAndStudentId(taskId, studentId).stream()
            .map(report -> {
                ReportDTO dto = new ReportDTO();
                dto.setId(report.getId());
                dto.setComment(report.getComment());
                dto.setGrade(report.getGrade());
                dto.setStatus(report.getStatus());
                dto.setSubmittedAt(report.getSubmittedAt());

                dto.setTaskId(report.getTask().getId());
                dto.setTaskTitle(report.getTask().getTitle());

                dto.setStudentId(report.getStudent().getId());
                dto.setStudentName(report.getStudent().getUser().getFullName());

                if (report.getSubmittedByUser() != null) {
                    dto.setSubmittedByUserName(report.getSubmittedByUser().getFullName());
                }

                dto.setAttachments(
                        report.getAttachments().stream()
                                .map(att -> {
                                    ReportAttachmentDto attachmentDto = new ReportAttachmentDto();
                                    attachmentDto.setId(att.getId());
                                    attachmentDto.setFileName(att.getFileName());
                                    attachmentDto.setContentType(att.getContentType());
                                    return attachmentDto;
                                })
                                .toList()
                );

                return dto;
            })
            .toList();
}
    public ResponseEntity<byte[]> downloadAttachment(int attachmentId) {
        ReportAttachment attachment = reporAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(attachment.getFileData());
        }   
    public void updateGrate(GrageDTO grageDTO){
           Report report =reportRepository.findById(grageDTO.getReportId()).orElseThrow(() -> new RuntimeException("Отчет не найден"));
           TeacherProfile teacherProfile= teacherProfileRepository.findById(grageDTO.getTeacherId()).orElseThrow(() -> new RuntimeException("Отчет не найден"));
           report.setSubmittedByUser(teacherProfile.getUser());
           report.setGrade(grageDTO.getGrade());
           report.setStatus(ReportStatus.ACCEPTED);
           reportRepository.save(report);
    }
    public void checkedGrade(int id,
                        String comment,
                        List<MultipartFile> files){
        Report report =reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Отчет не найден"));
      
        if(comment !=null){
            report.setCommentTeacher(comment);
        }
        report.setStatus(ReportStatus.CHECKED);
         if (files != null && !files.isEmpty()) { 
            for (MultipartFile file : files) {  
            if (file != null && !file.isEmpty()) {
        try {
            ReportAttachment reportAttachment=new ReportAttachment();
            reportAttachment.setFileName(file.getOriginalFilename());
            reportAttachment.setContentType(file.getContentType());
            reportAttachment.setFileData(file.getBytes());
            reportAttachment.setReport(report);
                report.getAttachments().add(reportAttachment);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении файла");
        }
    }
    }
}
    reportRepository.save(report);
}
@Transactional(readOnly = true)
public List<DisciplineDto> getDisciplinesByGroupId(int groupId) {
    return scheduleRepository.findByGroupId(groupId).stream()
            .map(Schedule::getDiscipline)
            .distinct()
            .map(discipline -> {
                DisciplineDto dto = new DisciplineDto();
                dto.setId(discipline.getId());
                dto.setName(discipline.getName());
                dto.setDescription(discipline.getDescription());
                dto.setUrlList(discipline.getUrlList());
                dto.setUmmfiles(
                   discipline.getUmmFiles().stream().map(
                    att ->{
                        UmmDTO ummDTO= new UmmDTO();
                        ummDTO.setFileName(att.getFileName());
                        ummDTO.setId(att.getId());
                        ummDTO.setFileType(att.getFileType());
                        return ummDTO;
                    }).toList() 
                );
                return dto;
            })
            .toList();
}
}
