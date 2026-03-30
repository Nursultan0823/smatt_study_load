package com.example.smatt_study_load.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "task_attachments")
public class TaskAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String fileName;

    private String contentType;

    @Lob
    @Column(name = "file_data")
    private byte[] fileData;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
}