package com.example.smatt_study_load.controllers;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.UmmMaterialDto;
import com.example.smatt_study_load.DTO.UmmMaterialShortDto;
import com.example.smatt_study_load.service.UmmMaterialService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/umm")
@AllArgsConstructor
public class UmmController {

    private final UmmMaterialService service;

    @GetMapping
    public List<UmmMaterialShortDto> list(
            @RequestParam(required = false) Integer disciplineId,
            @RequestParam(required = false) Integer authorId,
            @RequestParam(required = false) String search) {
        return service.search(disciplineId, authorId, search);
    }

    @GetMapping("/{id}")
    public UmmMaterialDto getOne(@PathVariable int id) {
        return service.getById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam int disciplineId,
            @RequestParam int authorId,
            @RequestParam(required = false) List<String> urls,
            @RequestParam(required = false) List<MultipartFile> files) {
        UmmMaterialDto dto = service.create(title, description, disciplineId, authorId, urls, files);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable int id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer disciplineId,
            @RequestParam(required = false) List<String> urls,
            @RequestParam(required = false) List<MultipartFile> files) {
        UmmMaterialDto dto = service.update(id, title, description, disciplineId, urls, files);
        return ResponseEntity.ok(dto);
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
