package com.example.smatt_study_load.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.smatt_study_load.DTO.DepartmentMethodicalAttachmentDto;
import com.example.smatt_study_load.DTO.DepartmentMethodicalMaterialDto;
import com.example.smatt_study_load.DTO.DepartmentMethodicalMaterialShortDto;
import com.example.smatt_study_load.models.DepartmentMethodicalMaterial;
import com.example.smatt_study_load.models.DepartmentMethodicalMaterialAttachment;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.DepartmentMethodicalMaterialAttachmentRepository;
import com.example.smatt_study_load.repository.DepartmentMethodicalMaterialRepository;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.UserDetailsImpl;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DepartmentMethodicalMaterialService {

    private static final String FALLBACK_FILE_TYPE = "application/octet-stream";

    private final DepartmentMethodicalMaterialRepository materialRepository;
    private final DepartmentMethodicalMaterialAttachmentRepository attachmentRepository;
    private final DisciplineRepository disciplineRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DepartmentMethodicalMaterialShortDto> search(Integer disciplineId, String search) {
        return materialRepository.search(disciplineId, normalizeSearch(search)).stream()
                .map(this::toShortDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long count() {
        return materialRepository.count();
    }

    @Transactional(readOnly = true)
    public DepartmentMethodicalMaterialDto getById(int id) {
        DepartmentMethodicalMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Методическое указание не найдено"));
        return toDto(material);
    }

    @Transactional
    public DepartmentMethodicalMaterialDto create(String title,
                                                 String description,
                                                 Integer disciplineId,
                                                 List<String> urls,
                                                 List<MultipartFile> files,
                                                 Authentication authentication) {
        requireText(title, "Название методического указания обязательно");

        User uploadedBy = getAuthenticatedUser(authentication);
        Discipline discipline = resolveDiscipline(disciplineId);

        DepartmentMethodicalMaterial material = new DepartmentMethodicalMaterial();
        material.setTitle(title.trim());
        material.setDescription(normalizeOptionalText(description));
        material.setCreatedAt(LocalDateTime.now());
        material.setUpdatedAt(LocalDateTime.now());
        material.setDiscipline(discipline);
        material.setUploadedBy(uploadedBy);
        material.setUrlList(new ArrayList<>());
        addUrls(material, urls);
        attachFiles(material, files);

        DepartmentMethodicalMaterial saved = materialRepository.save(material);
        return toDto(saved);
    }

    @Transactional
    public DepartmentMethodicalMaterialDto update(int id,
                                                 String title,
                                                 String description,
                                                 Integer disciplineId,
                                                 boolean clearDiscipline,
                                                 List<String> urls,
                                                 List<MultipartFile> files) {
        DepartmentMethodicalMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Методическое указание не найдено"));

        if (title != null) {
            requireText(title, "Название методического указания обязательно");
            material.setTitle(title.trim());
        }

        if (description != null) {
            material.setDescription(normalizeOptionalText(description));
        }

        if (clearDiscipline) {
            material.setDiscipline(null);
        } else if (disciplineId != null) {
            material.setDiscipline(resolveDiscipline(disciplineId));
        }

        addUrls(material, urls);
        attachFiles(material, files);
        material.setUpdatedAt(LocalDateTime.now());

        DepartmentMethodicalMaterial saved = materialRepository.save(material);
        return toDto(saved);
    }

    @Transactional
    public void delete(int id) {
        DepartmentMethodicalMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Методическое указание не найдено"));
        materialRepository.delete(material);
    }

    @Transactional
    public void removeUrl(int id, String url) {
        DepartmentMethodicalMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Методическое указание не найдено"));
        material.getUrlList().remove(url);
        material.setUpdatedAt(LocalDateTime.now());
        materialRepository.save(material);
    }

    @Transactional
    public void deleteAttachment(int attachmentId) {
        DepartmentMethodicalMaterialAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));
        DepartmentMethodicalMaterial material = attachment.getMaterial();
        if (material != null) {
            material.getAttachments().remove(attachment);
            material.setUpdatedAt(LocalDateTime.now());
        }
        attachmentRepository.delete(attachment);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadAttachment(int attachmentId) {
        DepartmentMethodicalMaterialAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));

        String fileType = normalizeFileType(attachment.getFileType());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(attachment.getFileData());
    }

    private Discipline resolveDiscipline(Integer disciplineId) {
        if (disciplineId == null) {
            return null;
        }

        return disciplineRepository.findById(disciplineId)
                .orElseThrow(() -> new RuntimeException("Дисциплина не найдена"));
    }

    private void addUrls(DepartmentMethodicalMaterial material, List<String> urls) {
        if (urls == null) {
            return;
        }

        for (String url : urls) {
            if (url != null && !url.isBlank()) {
                material.getUrlList().add(url.trim());
            }
        }
    }

    private void attachFiles(DepartmentMethodicalMaterial material, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            try {
                DepartmentMethodicalMaterialAttachment attachment =
                        new DepartmentMethodicalMaterialAttachment();
                attachment.setFileName(normalizeFileName(file.getOriginalFilename()));
                attachment.setFileType(normalizeFileType(file.getContentType()));
                attachment.setFileData(file.getBytes());
                attachment.setMaterial(material);
                material.getAttachments().add(attachment);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка при чтении файла");
            }
        }
    }

    private DepartmentMethodicalMaterialShortDto toShortDto(DepartmentMethodicalMaterial material) {
        DepartmentMethodicalMaterialShortDto dto = new DepartmentMethodicalMaterialShortDto();
        dto.setId(material.getId());
        dto.setTitle(material.getTitle());
        dto.setDescription(material.getDescription());
        dto.setCreatedAt(material.getCreatedAt());
        dto.setUpdatedAt(material.getUpdatedAt());
        if (material.getDiscipline() != null) {
            dto.setDisciplineId(material.getDiscipline().getId());
            dto.setDisciplineName(material.getDiscipline().getName());
        }
        dto.setUploadedById(material.getUploadedBy().getId());
        dto.setUploadedByName(material.getUploadedBy().getFullName());
        dto.setAttachmentsCount(material.getAttachments() != null ? material.getAttachments().size() : 0);
        dto.setUrlsCount(material.getUrlList() != null ? material.getUrlList().size() : 0);
        return dto;
    }

    private DepartmentMethodicalMaterialDto toDto(DepartmentMethodicalMaterial material) {
        DepartmentMethodicalMaterialDto dto = new DepartmentMethodicalMaterialDto();
        dto.setId(material.getId());
        dto.setTitle(material.getTitle());
        dto.setDescription(material.getDescription());
        dto.setCreatedAt(material.getCreatedAt());
        dto.setUpdatedAt(material.getUpdatedAt());
        if (material.getDiscipline() != null) {
            dto.setDisciplineId(material.getDiscipline().getId());
            dto.setDisciplineName(material.getDiscipline().getName());
        }
        dto.setUploadedById(material.getUploadedBy().getId());
        dto.setUploadedByName(material.getUploadedBy().getFullName());
        dto.setUrls(material.getUrlList() != null ? new ArrayList<>(material.getUrlList()) : new ArrayList<>());
        dto.setAttachments(
                material.getAttachments().stream()
                        .map(this::toAttachmentDto)
                        .toList()
        );
        return dto;
    }

    private DepartmentMethodicalAttachmentDto toAttachmentDto(DepartmentMethodicalMaterialAttachment attachment) {
        DepartmentMethodicalAttachmentDto dto = new DepartmentMethodicalAttachmentDto();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileType(attachment.getFileType());
        return dto;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
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

        String trimmed = search.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeFileType(String fileType) {
        return fileType == null || fileType.isBlank() ? FALLBACK_FILE_TYPE : fileType;
    }

    private static String normalizeFileName(String fileName) {
        return fileName == null || fileName.isBlank() ? "material-file" : fileName;
    }
}
