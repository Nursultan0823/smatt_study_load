package com.example.smatt_study_load.DTO;

import java.time.LocalDateTime;
import java.util.List;

import com.example.smatt_study_load.enums.ReportStatus;


import lombok.Data;

@Data

public class ReportDTO {
     private int id;
    private String comment;
    private Integer grade;
    private ReportStatus status;
    private LocalDateTime submittedAt;

    private int taskId;
    private String taskTitle;

    private int studentId;
    private String studentName;

    private String submittedByUserName;
    private List<ReportAttachmentDto> attachments;
}
