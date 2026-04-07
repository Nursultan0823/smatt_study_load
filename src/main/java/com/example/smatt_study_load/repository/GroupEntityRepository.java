package com.example.smatt_study_load.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.smatt_study_load.DTO.GetgroupDTO;
import com.example.smatt_study_load.DTO.GroupShortDto;
import com.example.smatt_study_load.models.GroupEntity;

public interface GroupEntityRepository extends JpaRepository<GroupEntity,Integer>{
        @Query("SELECT new com.example.smatt_study_load.DTO.GroupShortDto(g.id, g.name) FROM GroupEntity g")
    List<GroupShortDto> findAllGroupShort();
    @Query("SELECT new com.example.smatt_study_load.DTO.GetgroupDTO(g.id, g.name, g.courseNumber,g.specialty) FROM GroupEntity g")
    List<GetgroupDTO> findAllGetgroup();
       Optional<GroupEntity> findByName(String name);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, int id);
}
