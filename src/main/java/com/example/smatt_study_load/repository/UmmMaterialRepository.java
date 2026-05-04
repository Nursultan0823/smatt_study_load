package com.example.smatt_study_load.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.smatt_study_load.models.UmmMaterial;

public interface UmmMaterialRepository extends JpaRepository<UmmMaterial, Integer> {

    /**
     * Поиск материалов по фильтрам. Native + ILIKE для кириллицы в PostgreSQL;
     * поиск по названию, описанию, разделу и именам прикреплённых файлов.
     */
    @Query(value = """
            SELECT DISTINCT m.*
            FROM umm_materials m
            WHERE (CAST(:disciplineId AS INTEGER) IS NULL OR m.discipline_id = :disciplineId)
              AND (CAST(:authorId AS INTEGER) IS NULL OR m.author_id = :authorId)
              AND (
                  CAST(:search AS TEXT) IS NULL OR :search = ''
                  OR m.title ILIKE '%' || :search || '%'
                  OR COALESCE(m.description, '') ILIKE '%' || :search || '%'
                  OR COALESCE(m.section, '') ILIKE '%' || :search || '%'
                  OR EXISTS (
                      SELECT 1 FROM umm_material_attachments a
                      WHERE a.material_id = m.id
                        AND a.file_name ILIKE '%' || :search || '%'
                  )
              )
              AND (CAST(:materialKind AS TEXT) IS NULL OR :materialKind = ''
                  OR m.material_kind = :materialKind)
              AND (CAST(:sectionFilter AS TEXT) IS NULL OR :sectionFilter = ''
                  OR m.section = :sectionFilter)
            ORDER BY m.created_at DESC
            """, nativeQuery = true)
    List<UmmMaterial> search(
            @Param("disciplineId") Integer disciplineId,
            @Param("authorId") Integer authorId,
            @Param("search") String search,
            @Param("materialKind") String materialKind,
            @Param("sectionFilter") String sectionFilter);

    @Query("""
            select distinct m.section from UmmMaterial m
            where m.discipline.id = :disciplineId
              and m.section is not null
              and m.section <> ''
            order by m.section
            """)
    List<String> findDistinctSectionsByDisciplineId(@Param("disciplineId") int disciplineId);

    @Query("""
            select new com.example.smatt_study_load.DTO.UmmDisciplineStatDto(
                d.id, d.name, count(m))
            from UmmMaterial m join m.discipline d
            group by d.id, d.name
            order by d.name
            """)
    List<com.example.smatt_study_load.DTO.UmmDisciplineStatDto> summarizeByDiscipline();
}
