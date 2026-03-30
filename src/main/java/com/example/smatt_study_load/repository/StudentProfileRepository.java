package com.example.smatt_study_load.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.User;

public interface StudentProfileRepository extends JpaRepository<StudentProfile,Integer> {
    Optional<StudentProfile>findByUser(User user);
}
