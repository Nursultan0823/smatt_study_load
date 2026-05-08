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
import com.example.smatt_study_load.DTO.GetDisciplineDTO;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.TeacherDetailDto;
import com.example.smatt_study_load.DTO.TeacherDirectoryItemDto;
import com.example.smatt_study_load.DTO.TeacherScheduleDto;
import com.example.smatt_study_load.DTO.UmmDTO;
import com.example.smatt_study_load.DTO.UmmMaterialShortDto;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.Schedule;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.UmmFile;
import com.example.smatt_study_load.models.UmmMaterial;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;
import com.example.smatt_study_load.repository.UmmFileRepository;
import com.example.smatt_study_load.repository.UmmMaterialRepository;
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
        private final UmmMaterialRepository ummMaterialRepository;
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

    @Transactional(readOnly = true)
    public List<TeacherDirectoryItemDto> getTeacherDirectory() {
        return teacherProfileRepository
                .findByUser_EnabledTrueAndUser_StatusOrderByUser_FullNameAsc(UserStatus.APPROVED)
                .stream()
                .map(teacher -> new TeacherDirectoryItemDto(
                        teacher.getId(),
                        getTeacherName(teacher),
                        teacher.getUser() != null ? teacher.getUser().getEmail() : "",
                        teacher.getPosition(),
                        scheduleRepository.findDisciplinesByTeacherId(teacher.getId()).size(),
                        Math.toIntExact(ummMaterialRepository.countByAuthor_Id(teacher.getId()))
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public TeacherDetailDto getTeacherDetail(int teacherId) {
        TeacherProfile teacher = teacherProfileRepository
                .findByIdAndUser_EnabledTrueAndUser_Status(teacherId, UserStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        List<GetDisciplineDTO> disciplines = scheduleRepository.findDisciplinesByTeacherId(teacherId).stream()
                .distinct()
                .map(discipline -> new GetDisciplineDTO(
                        discipline.getId(),
                        discipline.getName(),
                        discipline.getDescription()
                ))
                .toList();

        List<TeacherScheduleDto> schedules = scheduleRepository
                .findByTeacherIdOrderByDayOfWeekAscStartTimeAsc(teacherId)
                .stream()
                .map(this::toTeacherScheduleDto)
                .toList();

        List<UmmMaterialShortDto> recentMaterials = ummMaterialRepository
                .findTop4ByAuthor_IdOrderByCreatedAtDesc(teacherId)
                .stream()
                .map(this::toUmmMaterialShortDto)
                .toList();

        return new TeacherDetailDto(
                teacher.getId(),
                getTeacherName(teacher),
                teacher.getUser() != null ? teacher.getUser().getEmail() : "",
                teacher.getPosition(),
                teacher.getUser() != null && teacher.getUser().isEnabled(),
                disciplines.size(),
                Math.toIntExact(ummMaterialRepository.countByAuthor_Id(teacherId)),
                disciplines,
                schedules,
                recentMaterials
        );
    }

    private TeacherScheduleDto toTeacherScheduleDto(Schedule schedule) {
        TeacherScheduleDto dto = new TeacherScheduleDto();
        dto.setId(schedule.getId());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setRoom(schedule.getRoom());
        dto.setUrl(schedule.getUrl());
        dto.setDisciplineName(schedule.getDiscipline().getName());
        dto.setGroupName(schedule.getGroup().getName());
        return dto;
    }

    private UmmMaterialShortDto toUmmMaterialShortDto(UmmMaterial material) {
        UmmMaterialShortDto dto = new UmmMaterialShortDto();
        dto.setId(material.getId());
        dto.setTitle(material.getTitle());
        dto.setDescription(material.getDescription());
        dto.setCreatedAt(material.getCreatedAt());
        dto.setUpdatedAt(material.getUpdatedAt());
        dto.setDisciplineId(material.getDiscipline().getId());
        dto.setDisciplineName(material.getDiscipline().getName());
        dto.setAuthorId(material.getAuthor().getId());
        dto.setAuthorName(getTeacherName(material.getAuthor()));
        dto.setMaterialKind(material.getMaterialKind().name());
        dto.setSection(material.getSection());
        dto.setAttachmentsCount(material.getAttachments() != null ? material.getAttachments().size() : 0);
        dto.setUrlsCount(material.getUrlList() != null ? material.getUrlList().size() : 0);
        return dto;
    }

    private String getTeacherName(TeacherProfile teacher) {
        if (teacher == null || teacher.getUser() == null) {
            return "Неизвестно";
        }
        return teacher.getUser().getFullName();
    }
}
