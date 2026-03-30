package com.example.smatt_study_load.repository;

import com.example.smatt_study_load.enums.Role;
import com.example.smatt_study_load.models.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Roles, Integer> {
  boolean existsByName(Role name);
    Optional<Roles> findByName(Role name);
}