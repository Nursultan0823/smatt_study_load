package com.example.smatt_study_load.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.smatt_study_load.DTO.UmmMaterialAttachmentDto;
import com.example.smatt_study_load.DTO.UmmMaterialDto;
import com.example.smatt_study_load.DTO.UmmMaterialShortDto;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.UmmMaterial;
import com.example.smatt_study_load.models.UmmMaterialAttachment;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;
import com.example.smatt_study_load.repository.UmmMaterialAttachmentRepository;
import com.example.smatt_study_load.repository.UmmMaterialRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UmmMaterialService {

    private final UmmMaterialRepository materialRepository;
    private final UmmMaterialAttachmentRepository attachmentRepository;
    private final DisciplineRepository disciplineRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    @Transactional(readOnly = true)
    public List<UmmMaterialShortDto> search(Integer disciplineId, Integer authorId, String search) {
        return materialRepository.search(disciplineId, authorId, search).stream()
                .map(this::toShortDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UmmMaterialDto getById(int id) {
        UmmMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Материал не найден"));
        return toDto(material);
    }

    @Transactional
    public UmmMaterialDto create(String title,
                                 String description,
                                 int disciplineId,
                                 int authorId,
                                 List<String> urls,
                                 List<MultipartFile> files) {
        Discipline discipline = disciplineRepository.findById(disciplineId)
                .orElseThrow(() -> new RuntimeException("Дисциплина не найдена"));
        TeacherProfile author = teacherProfileRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        UmmMaterial material = new UmmMaterial();
        material.setTitle(title);
        material.setDescription(description);
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
