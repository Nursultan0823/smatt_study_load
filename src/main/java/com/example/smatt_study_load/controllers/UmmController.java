package com.example.smatt_study_load.controllers;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.smatt_study_load.DTO.DepartmentMethodicalMaterialDto;
import com.example.smatt_study_load.DTO.DepartmentMethodicalMaterialShortDto;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.UmmDisciplineStatDto;
import com.example.smatt_study_load.DTO.UmmMaterialDto;
import com.example.smatt_study_load.DTO.UmmMaterialShortDto;
import com.example.smatt_study_load.models.UmmMaterialKind;
import com.example.smatt_study_load.service.DepartmentMethodicalMaterialService;
import com.example.smatt_study_load.service.UmmMaterialService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/umm")
@AllArgsConstructor
public class UmmController {

    private final UmmMaterialService service;
    private final DepartmentMethodicalMaterialService methodicalService;

    @GetMapping("/meta/discipline-stats")
    public List<UmmDisciplineStatDto> disciplineStats(Authentication authentication) {
        return service.disciplineStats(authentication);
    }

    @GetMapping("/meta/sections")
    public List<String> sections(@RequestParam int disciplineId) {
        return service.sectionsForDiscipline(disciplineId);
    }

    @GetMapping("/methodical/count")
    public long methodicalCount() {
        return methodicalService.count();
    }

    @GetMapping("/methodical")
    public List<DepartmentMethodicalMaterialShortDto> methodicalList(
            @RequestParam(required = false) Integer disciplineId,
            @RequestParam(required = false) String search) {
        return methodicalService.search(disciplineId, search);
    }

    @GetMapping("/methodical/{id}")
    public DepartmentMethodicalMaterialDto getMethodical(@PathVariable int id) {
        return methodicalService.getById(id);
    }

    @PostMapping(value = "/methodical", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMethodical(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer disciplineId,
            @RequestParam(required = false) List<String> urls,
            @RequestParam(required = false) List<MultipartFile> files,
            Authentication authentication) {
        DepartmentMethodicalMaterialDto dto = methodicalService.create(
                title,
                description,
                disciplineId,
                urls,
                files,
                authentication);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping(value = "/methodical/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMethodical(
            @PathVariable int id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer disciplineId,
            @RequestParam(required = false, defaultValue = "false") boolean clearDiscipline,
            @RequestParam(required = false) List<String> urls,
            @RequestParam(required = false) List<MultipartFile> files) {
        DepartmentMethodicalMaterialDto dto = methodicalService.update(
                id,
                title,
                description,
                disciplineId,
                clearDiscipline,
                urls,
                files);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/methodical/{id}")
    public ResponseEntity<?> deleteMethodical(@PathVariable int id) {
        methodicalService.delete(id);
        return ResponseEntity.ok(new Response("Методическое указание удалено"));
    }

    @DeleteMapping("/methodical/{id}/urls")
    public ResponseEntity<?> removeMethodicalUrl(@PathVariable int id, @RequestParam String url) {
        methodicalService.removeUrl(id, url);
        return ResponseEntity.ok(new Response("Ссылка удалена"));
    }

    @DeleteMapping("/methodical/attachments/{attachmentId}")
    public ResponseEntity<?> deleteMethodicalAttachment(@PathVariable int attachmentId) {
        methodicalService.deleteAttachment(attachmentId);
        return ResponseEntity.ok(new Response("Файл удален"));
    }

    @GetMapping("/methodical/attachments/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadMethodical(@PathVariable int attachmentId) {
        return methodicalService.downloadAttachment(attachmentId);
    }

    @GetMapping
    public List<UmmMaterialShortDto> list(
            @RequestParam(required = false) Integer disciplineId,
            @RequestParam(required = false) Integer authorId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String materialKind,
            @RequestParam(required = false) String section,
            Authentication authentication) {
        return service.search(
                disciplineId,
                authorId,
                search,
                parseKind(materialKind),
                section,
                authentication);
    }

    @GetMapping("/{id}")
    public UmmMaterialDto getOne(@PathVariable int id, Authentication authentication) {
        return service.getById(id, authentication);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam int disciplineId,
            @RequestParam int authorId,
            @RequestParam(required = false) String materialKind,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) List<String> urls,
            @RequestParam(required = false) List<MultipartFile> files) {
        UmmMaterialDto dto = service.create(
                title,
                description,
                disciplineId,
                authorId,
                parseKindOrDefault(materialKind),
                section,
                urls,
                files);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable int id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer disciplineId,
            @RequestParam(required = false) String materialKind,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) List<String> urls,
            @RequestParam(required = false) List<MultipartFile> files) {
        UmmMaterialDto dto = service.update(
                id,
                title,
                description,
                disciplineId,
                parseKind(materialKind),
                section,
                urls,
                files);
        return ResponseEntity.ok(dto);
    }

    private static UmmMaterialKind parseKindOrDefault(String raw) {
        UmmMaterialKind k = parseKind(raw);
        return k != null ? k : UmmMaterialKind.GENERAL;
    }

    private static UmmMaterialKind parseKind(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UmmMaterialKind.valueOf(raw.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.ok(new Response("Материал удален"));
    }

    @DeleteMapping("/{id}/urls")
    public ResponseEntity<?> removeUrl(@PathVariable int id, @RequestParam String url) {
        service.removeUrl(id, url);
        return ResponseEntity.ok(new Response("Ссылка удалена"));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(@PathVariable int attachmentId) {
        service.deleteAttachment(attachmentId);
        return ResponseEntity.ok(new Response("Файл удален"));
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<byte[]> download(@PathVariable int attachmentId) {
        return service.downloadAttachment(attachmentId);
    }
}
