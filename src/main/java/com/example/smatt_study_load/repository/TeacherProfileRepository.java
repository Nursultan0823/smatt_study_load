package com.example.smatt_study_load.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.User;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile,Integer> {
    Optional<TeacherProfile>findByUser(User user);
    Optional<TeacherProfile> findByUserEmail(String email);
}
