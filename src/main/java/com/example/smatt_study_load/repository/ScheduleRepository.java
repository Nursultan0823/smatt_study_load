package com.example.smatt_study_load.repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule,Integer> {
     List<Schedule> findByGroupId(int groupId);
        @Query("SELECT DISTINCT s.discipline FROM Schedule s WHERE s.teacher.id = :teacherId")
    List<Discipline> findDisciplinesByTeacherId(int teacherId);
     @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Schedule s
        WHERE s.group.id = :groupId
          AND s.dayOfWeek = :dayOfWeek
          AND s.startTime < :endTime
          AND s.endTime > :startTime
    """)
    boolean existsGroupConflict(
            int groupId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    );

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Schedule s
        WHERE s.teacher.id = :teacherId
          AND s.dayOfWeek = :dayOfWeek
          AND s.startTime < :endTime
          AND s.endTime > :startTime
    """)
    boolean existsTeacherConflict(
            int teacherId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    );

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Schedule s
        WHERE s.room = :room
          AND s.dayOfWeek = :dayOfWeek
          AND s.startTime < :endTime
          AND s.endTime > :startTime
    """)
    boolean existsRoomConflict(
            String room,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    );
}
