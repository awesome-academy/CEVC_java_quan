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
}
