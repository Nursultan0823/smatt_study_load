package com.example.smatt_study_load.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.TaskAnalyticsDto;
import com.example.smatt_study_load.DTO.TaskAttachmentDto;
import com.example.smatt_study_load.DTO.TaskDto;
import com.example.smatt_study_load.DTO.TaskStatisticsDto;
import com.example.smatt_study_load.enums.AnnouncementType;
import com.example.smatt_study_load.enums.ReportStatus;
import com.example.smatt_study_load.enums.Role;
import com.example.smatt_study_load.models.Announcement;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.GroupEntity;
import com.example.smatt_study_load.models.Report;
import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.Task;
import com.example.smatt_study_load.models.TaskAttachment;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.AnnouncementRepository;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.ReportRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.StudentProfileRepository;
import com.example.smatt_study_load.repository.TaskAttachmentRepository;
import com.example.smatt_study_load.repository.TaskRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.UserDetailsImpl;
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
        private final UserRepository userRepository;
        private final StudentProfileRepository studentProfileRepository;
        private final ReportRepository reportRepository;
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
@Transactional(readOnly = true)
public ResponseEntity<?> getTaskStatistics(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
        return ResponseEntity.status(401).body(new Response("Пользователь не авторизован"));
    }

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

    if (hasRole(user, Role.ADMIN)) {
        return ResponseEntity.ok(buildDeadlineStatistics(taskRepository.findAll()));
    }

    if (hasRole(user, Role.TEACHER)) {
        TeacherProfile teacher = teacherProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));
        return ResponseEntity.ok(
                buildTeacherStatistics(taskRepository.findByCreatedById(teacher.getId()))
        );
    }

    if (hasRole(user, Role.STUDENT) || hasRole(user, Role.GROUP_LEADER)) {
        StudentProfile student = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));
        return ResponseEntity.ok(buildStudentStatistics(student));
    }

    return ResponseEntity.status(403).body(new Response("Недостаточно прав"));
}

@Transactional(readOnly = true)
public ResponseEntity<?> getTaskAnalytics(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
        return ResponseEntity.status(401).body(new Response("Пользователь не авторизован"));
    }

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

    if (hasRole(user, Role.ADMIN)) {
        List<Task> tasks = taskRepository.findAll();
        return ResponseEntity.ok(buildAnalytics(tasks, collectReports(tasks)));
    }

    if (hasRole(user, Role.TEACHER)) {
        TeacherProfile teacher = teacherProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));
        List<Task> tasks = taskRepository.findByCreatedById(teacher.getId());
        return ResponseEntity.ok(buildAnalytics(tasks, collectReports(tasks)));
    }

    if (hasRole(user, Role.STUDENT) || hasRole(user, Role.GROUP_LEADER)) {
        StudentProfile student = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));
        List<Task> tasks = taskRepository.findAssignedToGroup(student.getGroup().getId());
        List<Report> reports = reportRepository.findByStudentId(student.getId());
        return ResponseEntity.ok(buildAnalytics(tasks, reports));
    }

    return ResponseEntity.status(403).body(new Response("Недостаточно прав"));
}

private TaskAnalyticsDto buildAnalytics(List<Task> tasks, List<Report> reports) {
    return new TaskAnalyticsDto(
            buildSubmissionOverview(tasks, reports),
            buildWeeklySubmissions(reports),
            buildDisciplineAverageGrades(reports),
            buildTopStudents(reports)
    );
}

private List<Report> collectReports(List<Task> tasks) {
    return tasks.stream()
            .flatMap(task -> task.getReports().stream())
            .toList();
}

private TaskAnalyticsDto.SubmissionOverview buildSubmissionOverview(List<Task> tasks, List<Report> reports) {
    LocalDateTime now = LocalDateTime.now();
    long submitted = reports.stream()
            .filter(report -> report.getStatus() == ReportStatus.ACCEPTED)
            .count();
    long overdue = tasks.stream()
            .filter(task -> isTeacherTaskOverdue(task, now))
            .count();

    return new TaskAnalyticsDto.SubmissionOverview(submitted, overdue);
}

private List<TaskAnalyticsDto.WeeklySubmissions> buildWeeklySubmissions(List<Report> reports) {
    LocalDate monday = LocalDate.now().minusWeeks(7).with(java.time.DayOfWeek.MONDAY);
    DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("dd.MM");

    Map<LocalDate, Long> countsByWeek = reports.stream()
            .filter(report -> report.getSubmittedAt() != null)
            .map(report -> report.getSubmittedAt().toLocalDate())
            .filter(date -> !date.isBefore(monday))
            .collect(Collectors.groupingBy(
                    date -> date.with(java.time.DayOfWeek.MONDAY),
                    Collectors.counting()
            ));

    List<TaskAnalyticsDto.WeeklySubmissions> result = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
        LocalDate weekStart = monday.plusWeeks(i);
        result.add(new TaskAnalyticsDto.WeeklySubmissions(
                weekStart.toString(),
                weekStart.format(labelFormatter),
                countsByWeek.getOrDefault(weekStart, 0L)
        ));
    }

    return result;
}

private List<TaskAnalyticsDto.DisciplineAverageGrade> buildDisciplineAverageGrades(List<Report> reports) {
    return reports.stream()
            .filter(report -> report.getGrade() != null)
            .collect(Collectors.groupingBy(report -> report.getTask().getDiscipline()))
            .entrySet()
            .stream()
            .map(entry -> new TaskAnalyticsDto.DisciplineAverageGrade(
                    entry.getKey().getId(),
                    entry.getKey().getName(),
                    roundAverage(entry.getValue().stream().map(Report::getGrade).toList()),
                    entry.getValue().size()
            ))
            .sorted(Comparator.comparing(TaskAnalyticsDto.DisciplineAverageGrade::getAverageGrade).reversed())
            .toList();
}

private List<TaskAnalyticsDto.StudentPerformance> buildTopStudents(List<Report> reports) {
    return reports.stream()
            .filter(report -> report.getGrade() != null)
            .collect(Collectors.groupingBy(Report::getStudent))
            .entrySet()
            .stream()
            .map(entry -> new TaskAnalyticsDto.StudentPerformance(
                    entry.getKey().getId(),
                    entry.getKey().getUser().getFullName(),
                    roundAverage(entry.getValue().stream().map(Report::getGrade).toList()),
                    entry.getValue().size()
            ))
            .sorted(Comparator
                    .comparing(TaskAnalyticsDto.StudentPerformance::getAverageGrade)
                    .reversed()
                    .thenComparing(TaskAnalyticsDto.StudentPerformance::getGradedReports, Comparator.reverseOrder()))
            .limit(5)
            .toList();
}

private double roundAverage(List<Integer> grades) {
    if (grades.isEmpty()) {
        return 0;
    }

    double average = grades.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0);
    return Math.round(average * 10.0) / 10.0;
}

private TaskStatisticsDto buildDeadlineStatistics(List<Task> tasks) {
    LocalDateTime now = LocalDateTime.now();
    long overdueTasks = tasks.stream()
            .filter(task -> isTeacherTaskOverdue(task, now))
            .count();

    return new TaskStatisticsDto(tasks.size(), overdueTasks, 0, 0, 0, 0, null);
}

private TaskStatisticsDto buildTeacherStatistics(List<Task> tasks) {
    LocalDateTime now = LocalDateTime.now();

    long overdueTasks = tasks.stream()
            .filter(task -> isTeacherTaskOverdue(task, now))
            .count();

    long tasksWithoutReports = tasks.stream()
            .filter(task -> task.getReports() == null || task.getReports().isEmpty())
            .count();

    long pendingReviewReports = tasks.stream()
            .flatMap(task -> task.getReports().stream())
            .filter(report -> report.getStatus() == ReportStatus.SUBMITTED)
            .count();

    long checkedReports = tasks.stream()
            .flatMap(task -> task.getReports().stream())
            .filter(report -> report.getStatus() == ReportStatus.CHECKED)
            .count();

    long acceptedReports = tasks.stream()
            .flatMap(task -> task.getReports().stream())
            .filter(report -> report.getStatus() == ReportStatus.ACCEPTED)
            .count();

    String nextDeadline = tasks.stream()
            .map(Task::getDeadline)
            .filter(deadline -> deadline != null && deadline.isAfter(now))
            .min(LocalDateTime::compareTo)
            .map(LocalDateTime::toString)
            .orElse(null);

    return new TaskStatisticsDto(
            tasks.size(),
            overdueTasks,
            tasksWithoutReports,
            pendingReviewReports,
            checkedReports,
            acceptedReports,
            nextDeadline
    );
}

private TaskStatisticsDto buildStudentStatistics(StudentProfile student) {
    List<Task> tasks = taskRepository.findAssignedToGroup(student.getGroup().getId());
    Map<Integer, Report> latestReportsByTask = new HashMap<>();

    reportRepository.findByStudentId(student.getId()).forEach(report -> {
        int taskId = report.getTask().getId();
        Report latestReport = latestReportsByTask.get(taskId);

        if (latestReport == null || isSubmittedAfter(report, latestReport)) {
            latestReportsByTask.put(taskId, report);
        }
    });

    LocalDateTime now = LocalDateTime.now();
    long overdueTasks = tasks.stream()
            .filter(task -> isStudentTaskOverdue(
                    task,
                    latestReportsByTask.get(task.getId()),
                    now
            ))
            .count();

    return new TaskStatisticsDto(tasks.size(), overdueTasks, 0, 0, 0, 0, null);
}

private boolean isDeadlinePassed(Task task, LocalDateTime now) {
    return task.getDeadline() != null && task.getDeadline().isBefore(now);
}

private boolean isTeacherTaskOverdue(Task task, LocalDateTime now) {
    if (!isDeadlinePassed(task, now)) {
        return false;
    }

    return task.getReports().stream()
            .noneMatch(report -> report.getStatus() == ReportStatus.ACCEPTED);
}

private boolean isStudentTaskOverdue(Task task, Report latestReport, LocalDateTime now) {
    if (!isDeadlinePassed(task, now)) {
        return false;
    }

    return latestReport == null || latestReport.getStatus() != ReportStatus.ACCEPTED;
}

private boolean isSubmittedAfter(Report report, Report latestReport) {
    if (report.getSubmittedAt() == null) {
        return false;
    }

    return latestReport.getSubmittedAt() == null
            || report.getSubmittedAt().isAfter(latestReport.getSubmittedAt());
}

private boolean hasRole(User user, Role role) {
    return user.getRoles().stream()
            .anyMatch(userRole -> userRole.getName() == role);
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
