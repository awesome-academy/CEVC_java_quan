package com.example.member_management_system.repository;

import com.example.member_management_system.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Project> findByNameIgnoreCase(String name);

    @Query(value = "SELECT p FROM Project p LEFT JOIN FETCH p.team LEFT JOIN FETCH p.leader",
            countQuery = "SELECT COUNT(p) FROM Project p")
    Page<Project> findAllWithDetails(Pageable pageable);

    @Query("SELECT p FROM Project p " +
            "LEFT JOIN FETCH p.team " +
            "LEFT JOIN FETCH p.leader " +
            "LEFT JOIN FETCH p.projectMembers pm " +
            "LEFT JOIN FETCH pm.member " +
            "LEFT JOIN FETCH pm.projectRole " +
            "WHERE p.id = :id")
    Optional<Project> findByIdWithDetails(@Param("id") Long id);
}
