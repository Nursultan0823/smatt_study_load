package com.example.smatt_study_load.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.smatt_study_load.models.UmmMaterial;

public interface UmmMaterialRepository extends JpaRepository<UmmMaterial, Integer> {

    /**
     * Поиск материалов по фильтрам. Используем native-запрос с ILIKE, чтобы
     * корректно работал регистронезависимый поиск по кириллице
     * (PostgreSQL с UTF-8 локалью).
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
              )
            ORDER BY m.created_at DESC
            """, nativeQuery = true)
    List<UmmMaterial> search(
            @Param("disciplineId") Integer disciplineId,
            @Param("authorId") Integer authorId,
            @Param("search") String search);
}
