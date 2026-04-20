package com.example.smatt_study_load.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smatt_study_load.models.UmmFile;

public interface UmmFileRepository extends JpaRepository<UmmFile,Integer> {
    
}
