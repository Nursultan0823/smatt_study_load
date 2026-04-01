package com.example.smatt_study_load.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule,Integer> {
     List<Schedule> findByGroupId(int groupId);
        @Query("SELECT DISTINCT s.discipline FROM Schedule s WHERE s.teacher.id = :teacherId")
    List<Discipline> findDisciplinesByTeacherId(int teacherId);
}
