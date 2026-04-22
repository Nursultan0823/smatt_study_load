package com.example.smatt_study_load.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.smatt_study_load.enums.ReportStatus;

@Data
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 3000)
    private String comment;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportAttachment> attachments = new ArrayList<>();

    private Integer grade;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private LocalDateTime submittedAt;
    private String commentTeacher;
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne
    @JoinColumn(name = "submitted_by_user_id")
    private User submittedByUser;

}
