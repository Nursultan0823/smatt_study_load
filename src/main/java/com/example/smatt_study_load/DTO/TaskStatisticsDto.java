package com.example.smatt_study_load.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatisticsDto {
    private long totalTasks;
    private long overdueTasks;
    private long tasksWithoutReports;
    private long pendingReviewReports;
    private long checkedReports;
    private long acceptedReports;
    private String nextDeadline;
}
