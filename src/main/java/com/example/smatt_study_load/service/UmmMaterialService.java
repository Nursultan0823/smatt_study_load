package com.example.smatt_study_load.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.smatt_study_load.DTO.UmmMaterialAttachmentDto;
import com.example.smatt_study_load.DTO.UmmMaterialDto;
import com.example.smatt_study_load.DTO.UmmMaterialShortDto;
import com.example.smatt_study_load.DTO.UmmDisciplineStatDto;
import com.example.smatt_study_load.enums.Role;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.Schedule;
import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.UmmMaterial;
import com.example.smatt_study_load.models.UmmMaterialKind;
import com.example.smatt_study_load.models.UmmMaterialAttachment;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.StudentProfileRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;
import com.example.smatt_study_load.repository.UmmMaterialAttachmentRepository;
import com.example.smatt_study_load.repository.UmmMaterialRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.UserDetailsImpl;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UmmMaterialService {

    private final UmmMaterialRepository materialRepository;
    private final UmmMaterialAttachmentRepository attachmentRepository;
    private final DisciplineRepository disciplineRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UmmMaterialShortDto> search(Integer disciplineId,
                                            Integer authorId,
                                            String search,
                                            UmmMaterialKind materialKind,
                                            String sectionFilter,
                                            Authentication authentication) {
        String searchNorm = normalizeSearch(search);
        String kindStr = materialKind != null ? materialKind.name() : null;
        String sectionEq = sectionFilter != null && !sectionFilter.isBlank() ? sectionFilter.trim() : null;

        List<Integer> disciplineIds = accessibleDisciplineIds(authentication);
        List<UmmMaterial> materials = disciplineIds == null
                ? materialRepository.search(
                        disciplineId,
                        authorId,
                        searchNorm,
                        kindStr,
                        sectionEq)
                : disciplineIds.isEmpty()
                        ? List.of()
                        : materialRepository.searchInDisciplines(
                                disciplineIds,
                                disciplineId,
                                authorId,
                                searchNorm,
                                kindStr,
                                sectionEq);

        return materials.stream()
                .map(this::toShortDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UmmDisciplineStatDto> disciplineStats(Authentication authentication) {
        List<Integer> disciplineIds = accessibleDisciplineIds(authentication);
        if (disciplineIds == null) {
            return materialRepository.summarizeByDiscipline();
        }
        if (disciplineIds.isEmpty()) {
            return List.of();
        }
        return materialRepository.summarizeByDisciplineIds(disciplineIds);
    }

    @Transactional(readOnly = true)
    public List<String> sectionsForDiscipline(int disciplineId) {
        return materialRepository.findDistinctSectionsByDisciplineId(disciplineId);
    }

    @Transactional(readOnly = true)
    public UmmMaterialDto getById(int id, Authentication authentication) {
        UmmMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Материал не найден"));
        authorizeMaterialView(material, authentication);
        return toDto(material);
    }

    private List<Integer> accessibleDisciplineIds(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isStudentAccess(user)) {
            return null;
        }

        StudentProfile student = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Студент не найден"));

        return scheduleRepository.findByGroupId(student.getGroup().getId()).stream()
                .map(Schedule::getDiscipline)
                .map(Discipline::getId)
                .distinct()
                .toList();
    }

    private void authorizeMaterialView(UmmMaterial material, Authentication authentication) {
        List<Integer> disciplineIds = accessibleDisciplineIds(authentication);
        if (disciplineIds != null && !disciplineIds.contains(material.getDiscipline().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Материал недоступен для текущей группы");
        }
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    private boolean isStudentAccess(User user) {
        boolean studentRole = user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.STUDENT || role.getName() == Role.GROUP_LEADER);
        boolean elevatedRole = user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.ADMIN || role.getName() == Role.TEACHER);
        return studentRole && !elevatedRole;
    }

    @Transactional
    public UmmMaterialDto create(String title,
                                 String description,
                                 int disciplineId,
                                 int authorId,
                                 UmmMaterialKind materialKind,
                                 String section,
                                 List<String> urls,
                                 List<MultipartFile> files) {
        requireText(title, "Название материала обязательно");
        requireText(description, "Описание материала обязательно");

        Discipline discipline = disciplineRepository.findById(disciplineId)
                .orElseThrow(() -> new RuntimeException("Дисциплина не найдена"));
        TeacherProfile author = teacherProfileRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        UmmMaterial material = new UmmMaterial();
        material.setTitle(title.trim());
        material.setDescription(description.trim());
        material.setMaterialKind(materialKind != null ? materialKind : UmmMaterialKind.GENERAL);
        material.setSection(normalizeSection(section));
        material.setCreatedAt(LocalDateTime.now());
        material.setUpdatedAt(LocalDateTime.now());
        material.setDiscipline(discipline);
        material.setAuthor(author);
        material.setUrlList(new ArrayList<>());

        if (urls != null) {
            for (String url : urls) {
                if (url != null && !url.isBlank()) {
                    material.getUrlList().add(url.trim());
                }
            }
        }

        attachFiles(material, files);

        UmmMaterial saved = materialRepository.save(material);
        return toDto(saved);
    }

    @Transactional
    public UmmMaterialDto update(int id,
                                 String title,
                                 String description,
                                 Integer disciplineId,
                                 UmmMaterialKind materialKind,
                                 String section,
                                 List<String> urls,
                                 List<MultipartFile> files) {
        UmmMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Материал не найден"));

        if (title != null && !title.isBlank()) {
            material.setTitle(title);
        }
        if (description != null) {
            material.setDescription(description);
        }
        if (disciplineId != null) {
            Discipline discipline = disciplineRepository.findById(disciplineId)
                    .orElseThrow(() -> new RuntimeException("Дисциплина не найдена"));
            material.setDiscipline(discipline);
        }
        if (materialKind != null) {
            material.setMaterialKind(materialKind);
        }
        if (section != null) {
            material.setSection(normalizeSection(section));
        }
        if (urls != null) {
            for (String url : urls) {
                if (url != null && !url.isBlank()) {
                    material.getUrlList().add(url.trim());
                }
            }
        }

        attachFiles(material, files);
        material.setUpdatedAt(LocalDateTime.now());

        UmmMaterial saved = materialRepository.save(material);
        return toDto(saved);
    }

    @Transactional
    public void delete(int id) {
        UmmMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Материал не найден"));
        materialRepository.delete(material);
    }

    @Transactional
    public void removeUrl(int id, String url) {
        UmmMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Материал не найден"));
        material.getUrlList().remove(url);
        material.setUpdatedAt(LocalDateTime.now());
        materialRepository.save(material);
    }

    @Transactional
    public void deleteAttachment(int attachmentId) {
        UmmMaterialAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));
        UmmMaterial material = attachment.getMaterial();
        if (material != null) {
            material.getAttachments().remove(attachment);
            material.setUpdatedAt(LocalDateTime.now());
        }
        attachmentRepository.delete(attachment);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadAttachment(int attachmentId) {
        UmmMaterialAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(attachment.getFileData());
    }

    private static String normalizeSection(String section) {
        if (section == null) {
            return null;
        }
        String t = section.trim();
        return t.isEmpty() ? null : t;
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private static String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }
        String t = search.trim();
        return t.isEmpty() ? null : t;
    }

    private void attachFiles(UmmMaterial material, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            try {
                UmmMaterialAttachment attachment = new UmmMaterialAttachment();
                attachment.setFileName(file.getOriginalFilename());
                attachment.setFileType(file.getContentType());
                attachment.setFileData(file.getBytes());
                attachment.setMaterial(material);
                material.getAttachments().add(attachment);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка при чтении файла");
            }
        }
    }

    private UmmMaterialShortDto toShortDto(UmmMaterial m) {
        UmmMaterialShortDto dto = new UmmMaterialShortDto();
        dto.setId(m.getId());
        dto.setTitle(m.getTitle());
        dto.setDescription(m.getDescription());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        dto.setDisciplineId(m.getDiscipline().getId());
        dto.setDisciplineName(m.getDiscipline().getName());
        dto.setAuthorId(m.getAuthor().getId());
        dto.setAuthorName(m.getAuthor().getUser().getFullName());
        dto.setMaterialKind(m.getMaterialKind() != null ? m.getMaterialKind().name() : UmmMaterialKind.GENERAL.name());
        dto.setSection(m.getSection());
        dto.setAttachmentsCount(m.getAttachments() != null ? m.getAttachments().size() : 0);
        dto.setUrlsCount(m.getUrlList() != null ? m.getUrlList().size() : 0);
        return dto;
    }

    private UmmMaterialDto toDto(UmmMaterial m) {
        UmmMaterialDto dto = new UmmMaterialDto();
        dto.setId(m.getId());
        dto.setTitle(m.getTitle());
        dto.setDescription(m.getDescription());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        dto.setDisciplineId(m.getDiscipline().getId());
        dto.setDisciplineName(m.getDiscipline().getName());
        dto.setAuthorId(m.getAuthor().getId());
        dto.setAuthorName(m.getAuthor().getUser().getFullName());
        dto.setMaterialKind(m.getMaterialKind() != null ? m.getMaterialKind().name() : UmmMaterialKind.GENERAL.name());
        dto.setSection(m.getSection());
        dto.setUrls(m.getUrlList() != null ? new ArrayList<>(m.getUrlList()) : new ArrayList<>());
        dto.setAttachments(
                m.getAttachments().stream().map(att -> {
                    UmmMaterialAttachmentDto a = new UmmMaterialAttachmentDto();
                    a.setId(att.getId());
                    a.setFileName(att.getFileName());
                    a.setFileType(att.getFileType());
                    return a;
                }).toList()
        );
        return dto;
    }
}
