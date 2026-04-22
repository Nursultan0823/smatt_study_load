package com.example.smatt_study_load.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smatt_study_load.models.Report;

public interface ReportRepository extends JpaRepository<Report,Integer> {
    List<Report> findByTaskId(int taskId);
    List<Report> findByTaskIdAndStudentId(int taskId, int studentId);
    List<Report> findByStudentId(int studentId);
}
