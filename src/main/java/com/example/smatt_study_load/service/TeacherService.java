package com.example.smatt_study_load.service;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.smatt_study_load.DTO.CurrentTeacherDTO;
import com.example.smatt_study_load.DTO.DisciplineDto;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.UmmDTO;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.UmmFile;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;
import com.example.smatt_study_load.repository.UmmFileRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.UserDetailsImpl;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TeacherService {
         private final UserRepository userRepository;
        private final TeacherProfileRepository teacherProfileRepository;
        private final ScheduleRepository scheduleRepository;
        private final DisciplineRepository disciplineRepository;
        private final UmmFileRepository ummFileRepository;
      public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body(new Response("Пользователь не авторизован"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        TeacherProfile teacherProfile=teacherProfileRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        CurrentTeacherDTO dto = new CurrentTeacherDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList(),
                teacherProfile.getPosition().toString(),
                teacherProfile.getId()
        );

        return ResponseEntity.ok(dto);
    }
     public List<Discipline> getDisciplinesByTeacherId(int teacherId) {
        return scheduleRepository.findDisciplinesByTeacherId(teacherId);
    }
  
    public void addUmm(int id,List<String>urls,List<MultipartFile>files){
        Discipline discipline=disciplineRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if(urls!=null && !urls.isEmpty()){
                for(String url: urls){
                        if(url != null && !url.isEmpty()){
                                discipline.getUrlList().add(url);
                        }
                }
        }
       
                  if (files != null && !files.isEmpty()) { 
            for (MultipartFile file : files) { 
            if (file != null && !file.isEmpty()) {
        try {
            UmmFile ummFile=new UmmFile();
            ummFile.setFileName(file.getOriginalFilename());
            ummFile.setFileType(file.getContentType());
            ummFile.setFileData(file.getBytes());
            discipline.getUmmFiles().add(ummFile);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении файла");
        }
       
        }
        }       
        }
     disciplineRepository.save(discipline);
    }
    public void removeUrlFromDiscipline(int disciplineId, String url) {
    Discipline discipline = disciplineRepository.findById(disciplineId)
            .orElseThrow(() -> new RuntimeException("Дисциплина не найдена"));

    discipline.getUrlList().remove(url);

    disciplineRepository.save(discipline);
}
public ResponseEntity<byte[]> downloadAttachment(int attachmentId) {
    UmmFile attachment = ummFileRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Файл не найден"));

    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(attachment.getFileType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + attachment.getFileName() + "\"")
            .body(attachment.getFileData());
    }

    @Transactional(readOnly = true)
public List<DisciplineDto> getDisciplinesTeacherId(int teacherId) {
    return scheduleRepository.findDisciplinesByTeacherId(teacherId).stream()
            .distinct()
            .map(discipline -> {
                DisciplineDto dto = new DisciplineDto();
                dto.setId(discipline.getId());
                dto.setName(discipline.getName());
                dto.setDescription(discipline.getDescription());
                dto.setUrlList(discipline.getUrlList());
                dto.setUmmfiles(
                        discipline.getUmmFiles().stream().map(att -> {
                            UmmDTO ummDTO = new UmmDTO();
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
