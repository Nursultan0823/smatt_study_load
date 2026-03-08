package com.example.smatt_study_load.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.User;

public interface UserRepository extends JpaRepository<User, Integer> {
 boolean existsByEmail(String email);
    List<User> findByStatus(UserStatus status);
    Optional<User> findByFullName(String fullname);
     Optional<User> findByEmail(String email);
   
}
