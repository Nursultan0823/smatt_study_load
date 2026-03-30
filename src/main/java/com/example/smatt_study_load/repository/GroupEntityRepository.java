package com.example.smatt_study_load.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.smatt_study_load.DTO.GroupShortDto;
import com.example.smatt_study_load.models.GroupEntity;

public interface GroupEntityRepository extends JpaRepository<GroupEntity,Integer>{
        @Query("SELECT new com.example.smatt_study_load.DTO.GroupShortDto(g.id, g.name) FROM GroupEntity g")
    List<GroupShortDto> findAllGroupShort();
}
