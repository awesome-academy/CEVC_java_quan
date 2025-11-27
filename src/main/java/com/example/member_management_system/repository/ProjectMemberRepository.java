package com.example.member_management_system.repository;

import com.example.member_management_system.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.member.id = :memberId AND pm.unassignedAt IS NULL")
    Optional<ProjectMember> findActiveMemberInProject(@Param("projectId") Long projectId, @Param("memberId") Long memberId);

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.unassignedAt IS NULL")
    List<ProjectMember> findByProjectIdAndUnassignedAtIsNull(@Param("projectId") Long projectId);

    /**
     * Find all active projects for a user by email
     * Fetches project, team, and project role in a single query
     */
    @Query("SELECT DISTINCT pm FROM ProjectMember pm " +
            "JOIN FETCH pm.project p " +
            "JOIN FETCH p.team t " +
            "JOIN FETCH pm.projectRole pr " +
            "WHERE pm.member.email = :email AND pm.unassignedAt IS NULL " +
            "ORDER BY pm.assignedAt DESC")
    List<ProjectMember> findActiveProjectsByMemberEmail(@Param("email") String email);

    /**
     * Find a specific project membership with all details for a user
     * Fetches project, team, leader, and project role in a single query
     */
    @Query("SELECT DISTINCT pm FROM ProjectMember pm " +
            "JOIN FETCH pm.project p " +
            "JOIN FETCH p.team t " +
            "LEFT JOIN FETCH p.leader l " +
            "LEFT JOIN FETCH l.position " +
            "JOIN FETCH pm.projectRole pr " +
            "WHERE pm.member.email = :email AND p.id = :projectId AND pm.unassignedAt IS NULL")
    Optional<ProjectMember> findProjectMembershipByEmailAndProjectId(
            @Param("email") String email,
            @Param("projectId") Long projectId
    );

    /**
     * Find all members of a project with their details
     * Fetches member, position, skills, and project role in a single query
     */
    @Query("SELECT DISTINCT pm FROM ProjectMember pm " +
            "JOIN FETCH pm.member m " +
            "LEFT JOIN FETCH m.position p " +
            "LEFT JOIN FETCH m.skills s " +
            "JOIN FETCH pm.projectRole pr " +
            "WHERE pm.project.id = :projectId " +
            "ORDER BY pm.unassignedAt ASC NULLS FIRST, pm.assignedAt DESC")
    List<ProjectMember> findProjectMembersWithDetails(@Param("projectId") Long projectId);
}
