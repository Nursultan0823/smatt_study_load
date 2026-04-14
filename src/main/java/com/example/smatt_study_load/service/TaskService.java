package com.example.smatt_study_load.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.example.smatt_study_load.DTO.TaskAttachmentDto;
import com.example.smatt_study_load.DTO.TaskDto;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.Task;
import com.example.smatt_study_load.models.TaskAttachment;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.TaskAttachmentRepository;
import com.example.smatt_study_load.repository.TaskRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TaskService {
        private final TeacherProfileRepository teacherProfileRepository;
        private final DisciplineRepository disciplineRepository;
        private final TaskRepository taskRepository;
        private final TaskAttachmentRepository taskAttachmentRepository;
    public void addTaskWithFile(String title,
                            String description,
                            int disciplineId,
                            int createdById,
                            MultipartFile file) {
    Discipline discipline = disciplineRepository.findById(disciplineId)
            .orElseThrow(() -> new RuntimeException("Дисциплина не найдена"));

    TeacherProfile teacher = teacherProfileRepository.findById(createdById)
            .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

    Task task = new Task();
    task.setTitle(title);
    task.setDescription(description);
    task.setDiscipline(discipline);
    task.setCreatedBy(teacher);

    if (file != null && !file.isEmpty()) {
        try {
            TaskAttachment attachment = new TaskAttachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setContentType(file.getContentType());
            attachment.setFileData(file.getBytes());
            attachment.setTask(task);

            task.getAttachments().add(attachment);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении файла");
        }
    }

    taskRepository.save(task);
}
public List<TaskDto> getTasksByDiscipline(int disciplineId) {
    return taskRepository.findByDisciplineId(disciplineId).stream()
            .map(task -> {
                TaskDto dto = new TaskDto();
                dto.setId(task.getId());
                dto.setTitle(task.getTitle());
                dto.setDescription(task.getDescription());
                dto.setDisciplineName(task.getDiscipline().getName());
                dto.setTeacherName(task.getCreatedBy().getUser().getFullName());

                dto.setAttachments(
                        task.getAttachments().stream()
                                .map(attachment -> {
                                    TaskAttachmentDto attachmentDto = new TaskAttachmentDto();
                                    attachmentDto.setId(attachment.getId());
                                    attachmentDto.setFileName(attachment.getFileName());
                                    attachmentDto.setContentType(attachment.getContentType());
                                    return attachmentDto;
                                })
                                .toList()
                );

                return dto;
            })
            .toList();
}
public ResponseEntity<byte[]> downloadAttachment(int attachmentId) {
    TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Файл не найден"));

    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(attachment.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + attachment.getFileName() + "\"")
            .body(attachment.getFileData());
}
public void updateTask(int taskId,
                       String title,
                       String description,
                       Integer disciplineId,
                       List<MultipartFile> files) {
    Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Задача не найдена"));

    if (title != null && !title.isBlank()) {
        task.setTitle(title);
    }

    if (description != null && !description.isBlank()) {
        task.setDescription(description);
    }

    if (disciplineId != null) {
        Discipline discipline = disciplineRepository.findById(disciplineId)
                .orElseThrow(() -> new RuntimeException("Дисциплина не найдена"));
        task.setDiscipline(discipline);
    }

    if (files != null && !files.isEmpty()) {
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                try {
                    TaskAttachment attachment = new TaskAttachment();
                    attachment.setFileName(file.getOriginalFilename());
                    attachment.setContentType(file.getContentType());
                    attachment.setFileData(file.getBytes());
                    attachment.setTask(task);

                    task.getAttachments().add(attachment);
                } catch (Exception e) {
                    throw new RuntimeException("Ошибка при сохранении файла");
                }
            }
        }
    }

    taskRepository.save(task);
}
public void deleteAttachment(int attachmentId) {
    TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Файл не найден"));

    taskAttachmentRepository.delete(attachment);
}
}
