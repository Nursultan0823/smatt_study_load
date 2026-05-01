package com.example.smatt_study_load.controllers;

import com.example.smatt_study_load.repository.TaskRepository;
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

import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.TaskDto;
import com.example.smatt_study_load.models.Task;
import com.example.smatt_study_load.service.TaskService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/task")
public class TaskController {
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addTaskWithFile(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam int disciplineId,
            @RequestParam int createdById,
            @RequestParam(required = false) String deadline,
            @RequestParam(required = false) List<MultipartFile> files
    ) {
        taskService.addTaskWithFiles(title, description, disciplineId, createdById, deadline, files);
        return ResponseEntity.ok(new Response("Задача с файлом добавлена"));
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getTaskStatistics(Authentication authentication) {
        return taskService.getTaskStatistics(authentication);
    }

    @GetMapping("/{disciplineId}")
public List<TaskDto> getTasksByDiscipline(@PathVariable int disciplineId) {
    return taskService.getTasksByDiscipline(disciplineId);
}
@GetMapping("/attachments/{attachmentId}/download")
public ResponseEntity<byte[]> downloadAttachment(@PathVariable int attachmentId) {
    return taskService.downloadAttachment(attachmentId);
}
@PatchMapping(value = "/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> updateTask(
        @PathVariable int taskId,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Integer disciplineId,
        @RequestParam(required = false) String deadline,
        @RequestParam(required = false) List<MultipartFile> files
) {
    taskService.updateTask(taskId, title, description, disciplineId, deadline, files);
    return ResponseEntity.ok(new Response("Задача обновлена"));
}
@DeleteMapping("/attachments/{attachmentId}")
public ResponseEntity<?> deleteAttachment(@PathVariable int attachmentId) {
    taskService.deleteAttachment(attachmentId);
    return ResponseEntity.ok(new Response("Файл удален"));
}
@DeleteMapping("/{taskId}")
public ResponseEntity<?> deleteTask(@PathVariable int taskId) {
   Task task= taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Файл не найден"));
   taskRepository.delete(task);
    return ResponseEntity.ok(new Response("задача удалена"));
}
}
