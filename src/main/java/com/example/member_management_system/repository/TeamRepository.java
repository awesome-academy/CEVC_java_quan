package com.example.member_management_system.repository;

import com.example.member_management_system.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Team> findByNameIgnoreCase(String name);

    // Fetch Leader to avoid N+1
    @Query(value = "SELECT t FROM Team t JOIN FETCH t.leader",
            countQuery = "SELECT COUNT(t) FROM Team t")
    Page<Team> findAllWithLeader(Pageable pageable);

    // Fetch Leader and current Members for Detail view
    @Query("SELECT t FROM Team t " +
            "JOIN FETCH t.leader " +
            "LEFT JOIN FETCH t.teamMembers tm " +
            "LEFT JOIN FETCH tm.member " +
            "LEFT JOIN FETCH tm.teamRole " +
            "WHERE t.id = :id")
    Optional<Team> findByIdWithDetails(@Param("id") Long id);
}
