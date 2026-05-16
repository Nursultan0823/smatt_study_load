package com.example.smatt_study_load.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.smatt_study_load.models.DepartmentMethodicalMaterial;

public interface DepartmentMethodicalMaterialRepository
        extends JpaRepository<DepartmentMethodicalMaterial, Integer> {

    @Query(value = """
            SELECT DISTINCT m.*
            FROM department_methodical_materials m
            LEFT JOIN disciplines d ON d.id = m.discipline_id
            WHERE (CAST(:disciplineId AS INTEGER) IS NULL OR m.discipline_id = :disciplineId)
              AND (
                  CAST(:search AS TEXT) IS NULL OR :search = ''
                  OR m.title ILIKE '%' || :search || '%'
                  OR COALESCE(m.description, '') ILIKE '%' || :search || '%'
                  OR COALESCE(d.name, '') ILIKE '%' || :search || '%'
                  OR EXISTS (
                      SELECT 1 FROM department_methodical_material_attachments a
                      WHERE a.material_id = m.id
                        AND a.file_name ILIKE '%' || :search || '%'
                  )
              )
            ORDER BY m.created_at DESC
            """, nativeQuery = true)
    List<DepartmentMethodicalMaterial> search(
            @Param("disciplineId") Integer disciplineId,
            @Param("search") String search
    );
}
