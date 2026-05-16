package com.example.smatt_study_load.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smatt_study_load.models.Report;

public interface ReportRepository extends JpaRepository<Report,Integer> {
    List<Report> findByTaskId(int taskId);
    List<Report> findByTaskIdAndStudentId(int taskId, int studentId);
    List<Report> findByStudentId(int studentId);
    List<Report> findByTaskIdOrderBySubmittedAtDescIdDesc(int taskId);
    List<Report> findByTaskIdAndStudentIdOrderBySubmittedAtDescIdDesc(int taskId, int studentId);
    List<Report> findByStudentIdOrderBySubmittedAtDescIdDesc(int studentId);
    Optional<Report> findFirstByTaskIdAndStudentIdOrderBySubmittedAtDescIdDesc(int taskId, int studentId);
}
