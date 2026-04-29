package com.example.smatt_study_load.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.example.smatt_study_load.DTO.TaskAttachmentDto;
import com.example.smatt_study_load.DTO.TaskDto;
import com.example.smatt_study_load.enums.AnnouncementType;
import com.example.smatt_study_load.models.Announcement;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.GroupEntity;
import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.Task;
import com.example.smatt_study_load.models.TaskAttachment;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.repository.AnnouncementRepository;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;
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
        private final ScheduleRepository scheduleRepository;
        private final AnnouncementRepository announcementRepository;
        private final EmailService emailService;
  @Transactional
public void addTaskWithFiles(String title,
                             String description,
                             int disciplineId,
                             int createdById,
                             String deadline,
                             List<MultipartFile> files) {

    Discipline discipline = disciplineRepository.findById(disciplineId)
            .orElseThrow(() -> new RuntimeException("Дисциплина не найдена"));

    TeacherProfile teacher = teacherProfileRepository.findById(createdById)
            .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

    List<GroupEntity> groups = scheduleRepository.findGroupsByDisciplineAndTeacher(
            disciplineId,
            createdById
    );

    if (groups.isEmpty()) {
        throw new RuntimeException("Для этой дисциплины и преподавателя не найдены группы в расписании");
    }

    Task task = new Task();
    task.setTitle(title);
    task.setDescription(description);
    task.setDiscipline(discipline);
    task.setCreatedBy(teacher);
    task.setCreatedAt(LocalDateTime.now());

    if (deadline != null && !deadline.isBlank()) {
        task.setDeadline(LocalDateTime.parse(deadline));
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
                    throw new RuntimeException("Ошибка при чтении файла");
                }
            }
        }
    }

    Task savedTask = taskRepository.save(task);

    Announcement announcement = new Announcement();
    announcement.setTitle("Новое задание");
    announcement.setContent("Преподаватель добавил новое задание: " + savedTask.getTitle());
    announcement.setDiscipline(discipline);
    announcement.setTeacher(teacher);
    announcement.setCreatedAt(LocalDateTime.now());
    announcement.setGroups(groups);
    announcement.setTargetId(savedTask.getId());
    announcement.setType(AnnouncementType.TASK_CREATED);
    announcementRepository.save(announcement);
    for (GroupEntity group : groups) {
    for (StudentProfile student : group.getStudents()) {
        String email = student.getUser().getEmail();

        if (email != null && !email.isBlank()) {
            emailService.sendSimpleEmail(
                    email,
                    "Новое задание",
                    "По дисциплине " + discipline.getName()
                            + " добавлено новое задание: " + savedTask.getTitle()
            );
        }
    }
}
}
@Transactional(readOnly = true)
public List<TaskDto> getTasksByDiscipline(int disciplineId) {
    return taskRepository.findByDisciplineId(disciplineId).stream()
            .map(task -> {
                TaskDto dto = new TaskDto();
                dto.setId(task.getId());
                dto.setTitle(task.getTitle());
                dto.setDescription(task.getDescription());
                dto.setDisciplineName(task.getDiscipline().getName());
                dto.setDisciplineId(task.getDiscipline().getId());
                dto.setTeacherName(task.getCreatedBy().getUser().getFullName());
                dto.setCreatedAt(task.getCreatedAt());
                dto.setDeadline(task.getDeadline());

                dto.setAttachments(
                        task.getAttachments().stream()
                                .map(att -> {
                                    TaskAttachmentDto a = new TaskAttachmentDto();
                                    a.setId(att.getId());
                                    a.setFileName(att.getFileName());
                                    a.setContentType(att.getContentType());
                                    return a;
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
                       String deadline,
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

    if (deadline != null) {
        if (deadline.isBlank()) {
            task.setDeadline(null);
        } else {
            task.setDeadline(LocalDateTime.parse(deadline));
        }
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
