package com.example.smatt_study_load.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smatt_study_load.models.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule,Integer> {
     List<Schedule> findByGroupId(int groupId);
}
