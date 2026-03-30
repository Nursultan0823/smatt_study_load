package com.example.smatt_study_load.models;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 3000)
    private String comment;

    private String fileName;

    @Lob
    @Column(name = "file_data")
    private byte[] fileData;

    private Integer grade;

    private String status; // SUBMITTED, CHECKED, ACCEPTED

    private LocalDateTime submittedAt;

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
