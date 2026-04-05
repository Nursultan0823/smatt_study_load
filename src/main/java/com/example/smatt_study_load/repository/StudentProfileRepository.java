package com.example.smatt_study_load.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.User;

public interface StudentProfileRepository extends JpaRepository<StudentProfile,Integer> {
    Optional<StudentProfile>findByUser(User user);

    @Query("""
        select sp
        from StudentProfile sp
        left join fetch sp.user
        left join fetch sp.group
        where sp.group.id = :groupId
    """)
    List<StudentProfile> findAllByGroupIdWithUserAndGroup(@Param("groupId") int groupId);
}
