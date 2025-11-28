package com.example.member_management_system.repository;

import com.example.member_management_system.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    // Search for current TeamMember by Member ID
    @Query("SELECT tm FROM TeamMember tm WHERE tm.member.id = :memberId AND tm.isCurrent = true")
    Optional<TeamMember> findCurrentByMemberId(@Param("memberId") Long memberId);

    List<TeamMember> findByTeamId(Long teamId);

    List<TeamMember> findByTeamIdAndIsCurrentTrue(Long teamId);

    /**
     * Find current team membership with all details for a user by email
     * Fetches team, team role, and leader in a single query to avoid N+1 problem
     */
    @Query("SELECT DISTINCT tm FROM TeamMember tm " +
            "JOIN FETCH tm.team t " +
            "JOIN FETCH t.leader l " +
            "LEFT JOIN FETCH l.position " +
            "JOIN FETCH tm.teamRole tr " +
            "WHERE tm.member.email = :email AND tm.isCurrent = true")
    Optional<TeamMember> findCurrentTeamByMemberEmail(@Param("email") String email);

    /**
     * Find all team members for a team with their details
     * Fetches member, position, skills, and team role in a single query
     */
    @Query("SELECT DISTINCT tm FROM TeamMember tm " +
            "JOIN FETCH tm.member m " +
            "LEFT JOIN FETCH m.position p " +
            "LEFT JOIN FETCH m.skills s " +
            "JOIN FETCH tm.teamRole tr " +
            "WHERE tm.team.id = :teamId " +
            "ORDER BY tm.isCurrent DESC, tm.startDate DESC")
    List<TeamMember> findByTeamIdWithDetails(@Param("teamId") Long teamId);
}
