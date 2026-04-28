package com.example.smatt_study_load.repository;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smatt_study_load.models.Task;

public interface TaskRepository extends JpaRepository<Task,Integer>{
     List<Task> findByDisciplineId(int disciplineId);
      List<Task> findByDeadlineBetween(LocalDateTime start, LocalDateTime end);
}
