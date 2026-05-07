package com.example.smatt_study_load.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAnalyticsDto {
    private SubmissionOverview submissionOverview;
    private List<WeeklySubmissions> weeklySubmissions;
    private List<DisciplineAverageGrade> disciplineAverageGrades;
    private List<StudentPerformance> topStudents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionOverview {
        private long submitted;
        private long overdue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklySubmissions {
        private String weekStart;
        private String label;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisciplineAverageGrade {
        private int disciplineId;
        private String disciplineName;
        private double averageGrade;
        private long gradedReports;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentPerformance {
        private int studentId;
        private String studentName;
        private double averageGrade;
        private long gradedReports;
    }
}
