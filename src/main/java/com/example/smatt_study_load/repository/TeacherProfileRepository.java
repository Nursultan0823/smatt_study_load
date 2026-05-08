package com.example.smatt_study_load.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.User;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile,Integer> {
    Optional<TeacherProfile>findByUser(User user);
    Optional<TeacherProfile> findByUserEmail(String email);
    List<TeacherProfile> findByUser_EnabledTrueAndUser_StatusOrderByUser_FullNameAsc(UserStatus status);
    Optional<TeacherProfile> findByIdAndUser_EnabledTrueAndUser_Status(int id, UserStatus status);
}
