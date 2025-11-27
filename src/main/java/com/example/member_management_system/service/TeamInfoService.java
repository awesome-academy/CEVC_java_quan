package com.example.member_management_system.service;

import com.example.member_management_system.dto.team.CurrentTeamResponse;
import com.example.member_management_system.dto.team.TeamMemberResponse;
import com.example.member_management_system.entity.TeamMember;
import com.example.member_management_system.exception.ResourceNotFoundException;
import com.example.member_management_system.repository.TeamMemberRepository;
import com.example.member_management_system.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamInfoService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    /**
     * Get current team information for the authenticated user
     * Fetches all data in a single optimized query to avoid N+1 problem
     *
     * @param email Current user's email from JWT
     * @return CurrentTeamResponse with team details
     */
    @Transactional(readOnly = true)
    public CurrentTeamResponse getCurrentTeam(String email) {
        // Fetch current team membership with all relationships in a single query
        TeamMember teamMember = teamMemberRepository.findCurrentTeamByMemberEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User is not currently assigned to any team"));

        // Count active members in the team
        long memberCount = teamMemberRepository.findByTeamIdAndIsCurrentTrue(teamMember.getTeam().getId()).size();

        // Build leader info
        CurrentTeamResponse.LeaderInfo leaderInfo = CurrentTeamResponse.LeaderInfo.builder()
                .id(teamMember.getTeam().getLeader().getId())
                .fullName(teamMember.getTeam().getLeader().getFullName())
                .email(teamMember.getTeam().getLeader().getEmail())
                .position(teamMember.getTeam().getLeader().getPosition() != null ?
                        teamMember.getTeam().getLeader().getPosition().getName() : null)
                .build();

        // Build response
        CurrentTeamResponse response = CurrentTeamResponse.builder()
                .teamId(teamMember.getTeam().getId())
                .teamName(teamMember.getTeam().getName())
                .teamDescription(teamMember.getTeam().getDescription())
                .leader(leaderInfo)
                .userRole(teamMember.getTeamRole().getName())
                .joinedDate(teamMember.getStartDate())
                .memberCount((int) memberCount)
                .build();

        log.info("Retrieved current team for user: {}", email);
        return response;
    }

    /**
     * Get list of members in a team
     * Fetches all member details in a single optimized query to avoid N+1 problem
     *
     * @param teamId Team ID
     * @return List of TeamMemberResponse
     */
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        // Verify team exists
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with ID: " + teamId);
        }

        // Fetch all team members with their details in a single query
        List<TeamMember> teamMembers = teamMemberRepository.findByTeamIdWithDetails(teamId);

        // Convert to response DTOs
        List<TeamMemberResponse> responses = teamMembers.stream()
                .map(this::buildTeamMemberResponse)
                .collect(Collectors.toList());

        log.info("Retrieved {} members for team ID: {}", responses.size(), teamId);
        return responses;
    }

    /**
     * Helper method to build TeamMemberResponse from TeamMember entity
     * Avoids code duplication
     *
     * @param teamMember TeamMember entity with all relationships loaded
     * @return TeamMemberResponse
     */
    private TeamMemberResponse buildTeamMemberResponse(TeamMember teamMember) {
        // Build position info
        TeamMemberResponse.PositionInfo positionInfo = null;
        if (teamMember.getMember().getPosition() != null) {
            positionInfo = TeamMemberResponse.PositionInfo.builder()
                    .id(teamMember.getMember().getPosition().getId())
                    .name(teamMember.getMember().getPosition().getName())
                    .abbreviation(teamMember.getMember().getPosition().getAbbreviation())
                    .build();
        }

        // Build skills list
        List<TeamMemberResponse.SkillInfo> skills = teamMember.getMember().getSkills().stream()
                .map(skill -> TeamMemberResponse.SkillInfo.builder()
                        .id(skill.getId())
                        .name(skill.getName())
                        .build())
                .collect(Collectors.toList());

        return TeamMemberResponse.builder()
                .memberId(teamMember.getMember().getId())
                .fullName(teamMember.getMember().getFullName())
                .email(teamMember.getMember().getEmail())
                .position(positionInfo)
                .teamRole(teamMember.getTeamRole().getName())
                .startDate(teamMember.getStartDate())
                .endDate(teamMember.getEndDate())
                .current(teamMember.isCurrent())
                .skills(skills)
                .build();
    }
}

