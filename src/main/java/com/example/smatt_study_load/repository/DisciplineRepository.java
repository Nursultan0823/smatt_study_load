package com.example.smatt_study_load.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.smatt_study_load.DTO.GetDisciplineDTO;
import com.example.smatt_study_load.models.Discipline;

public interface DisciplineRepository extends JpaRepository<Discipline,Integer> {
    @Query("SELECT new com.example.smatt_study_load.DTO.GetDisciplineDTO(g.id,g.name,g.description) FROM Discipline g")
    List<GetDisciplineDTO> findAllDisciplineDTO();
}
