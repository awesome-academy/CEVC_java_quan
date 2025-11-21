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
}
