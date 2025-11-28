package com.example.member_management_system.service;

import com.example.member_management_system.dto.project.ProjectDetailResponse;
import com.example.member_management_system.dto.project.UserProjectResponse;
import com.example.member_management_system.entity.ProjectMember;
import com.example.member_management_system.exception.ResourceNotFoundException;
import com.example.member_management_system.repository.ProjectMemberRepository;
import com.example.member_management_system.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectParticipationService {

    private final ProjectMemberRepository projectMemberRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ActivityLogService activityLogService;

    /**
     * Get all active projects that the user is participating in
     * Fetches all data in optimized queries to avoid N+1 problem
     *
     * @param email Current user's email from JWT
     * @return List of UserProjectResponse
     */
    @Transactional(readOnly = true)
    public List<UserProjectResponse> getUserProjects(String email) {
        // Fetch all active project memberships with details in a single query
        List<ProjectMember> projectMembers = projectMemberRepository.findActiveProjectsByMemberEmail(email);

        // If no projects, return empty list
        if (projectMembers.isEmpty()) {
            log.info("No active projects found for user: {}", email);
            return List.of();
        }

        // Batch fetch member counts for all projects to avoid N+1 query problem
        List<Long> projectIds = projectMembers.stream()
                .map(pm -> pm.getProject().getId())
                .collect(Collectors.toList());

        // Get member counts in a single batch query
        List<Object[]> memberCounts = projectMemberRepository.countActiveMembersByProjectIds(projectIds);

        // Convert to map for O(1) lookup: projectId -> count
        java.util.Map<Long, Long> memberCountMap = memberCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        // Convert to response DTOs using the pre-fetched counts
        List<UserProjectResponse> responses = projectMembers.stream()
                .map(pm -> buildUserProjectResponse(pm, memberCountMap))
                .collect(Collectors.toList());

        log.info("Retrieved {} active projects for user: {}", responses.size(), email);
        return responses;
    }

    /**
     * Get detailed information about a specific project
     * Fetches all data in optimized queries to avoid N+1 problem
     *
     * @param projectId Project ID
     * @param email     Current user's email from JWT
     * @return ProjectDetailResponse
     */
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectDetail(Long projectId, String email) {
        // Verify user is a member of the project and fetch user's membership details
        ProjectMember userMembership = projectMemberRepository
                .findProjectMembershipByEmailAndProjectId(email, projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found or you are not a member of this project"
                ));

        // Fetch all project members with their details in a single query
        List<ProjectMember> allMembers = projectMemberRepository.findProjectMembersWithDetails(projectId);

        // Count active team members
        long teamMemberCount = teamMemberRepository
                .findByTeamIdAndIsCurrentTrue(userMembership.getProject().getTeam().getId())
                .size();

        // Build response
        ProjectDetailResponse response = buildProjectDetailResponse(
                userMembership,
                allMembers,
                (int) teamMemberCount
        );

        // Log view activity
        activityLogService.logActivity(
                email,
                "VIEW",
                "User viewed project details: " + userMembership.getProject().getName(),
                "projects",
                projectId
        );

        log.info("Retrieved project detail for project ID: {} by user: {}", projectId, email);
        return response;
    }

    /**
     * Helper method to build UserProjectResponse from ProjectMember entity
     * Uses pre-fetched member count map to avoid N+1 queries
     *
     * @param projectMember  ProjectMember entity with all relationships loaded
     * @param memberCountMap Map of project ID to active member count
     * @return UserProjectResponse
     */
    private UserProjectResponse buildUserProjectResponse(
            ProjectMember projectMember,
            java.util.Map<Long, Long> memberCountMap
    ) {
        // Get member count from pre-fetched map (default to 0 if not found)
        int memberCount = memberCountMap.getOrDefault(
                projectMember.getProject().getId(),
                0L
        ).intValue();

        // Build team info
        UserProjectResponse.TeamInfo teamInfo = UserProjectResponse.TeamInfo.builder()
                .id(projectMember.getProject().getTeam().getId())
                .name(projectMember.getProject().getTeam().getName())
                .build();

        return UserProjectResponse.builder()
                .projectId(projectMember.getProject().getId())
                .projectName(projectMember.getProject().getName())
                .projectAbbreviation(projectMember.getProject().getAbbreviation())
                .startDate(projectMember.getProject().getStartDate())
                .endDate(projectMember.getProject().getEndDate())
                .status(projectMember.getProject().getStatus())
                .userRole(projectMember.getProjectRole().getName())
                .assignedAt(projectMember.getAssignedAt())
                .team(teamInfo)
                .memberCount(memberCount)
                .build();
    }

    /**
     * Helper method to build ProjectDetailResponse from ProjectMember entities
     * Avoids code duplication and redundant queries
     *
     * @param userMembership  Current user's project membership
     * @param allMembers      All project members
     * @param teamMemberCount Number of team members
     * @return ProjectDetailResponse
     */
    private ProjectDetailResponse buildProjectDetailResponse(
            ProjectMember userMembership,
            List<ProjectMember> allMembers,
            int teamMemberCount
    ) {
        // Build leader info
        ProjectDetailResponse.LeaderInfo leaderInfo = null;
        if (userMembership.getProject().getLeader() != null) {
            leaderInfo = ProjectDetailResponse.LeaderInfo.builder()
                    .id(userMembership.getProject().getLeader().getId())
                    .fullName(userMembership.getProject().getLeader().getFullName())
                    .email(userMembership.getProject().getLeader().getEmail())
                    .position(userMembership.getProject().getLeader().getPosition() != null ?
                            userMembership.getProject().getLeader().getPosition().getName() : null)
                    .build();
        }

        // Build team info
        ProjectDetailResponse.TeamInfo teamInfo = ProjectDetailResponse.TeamInfo.builder()
                .id(userMembership.getProject().getTeam().getId())
                .name(userMembership.getProject().getTeam().getName())
                .description(userMembership.getProject().getTeam().getDescription())
                .memberCount(teamMemberCount)
                .build();

        // Build members list
        List<ProjectDetailResponse.ProjectMemberInfo> memberInfoList = allMembers.stream()
                .map(this::buildProjectMemberInfo)
                .collect(Collectors.toList());

        return ProjectDetailResponse.builder()
                .projectId(userMembership.getProject().getId())
                .projectName(userMembership.getProject().getName())
                .projectAbbreviation(userMembership.getProject().getAbbreviation())
                .startDate(userMembership.getProject().getStartDate())
                .endDate(userMembership.getProject().getEndDate())
                .status(userMembership.getProject().getStatus())
                .leader(leaderInfo)
                .team(teamInfo)
                .userRole(userMembership.getProjectRole().getName())
                .userAssignedAt(userMembership.getAssignedAt())
                .members(memberInfoList)
                .build();
    }

    /**
     * Helper method to build ProjectMemberInfo from ProjectMember entity
     * Handles null values gracefully
     *
     * @param projectMember ProjectMember entity with all relationships loaded
     * @return ProjectMemberInfo
     */
    private ProjectDetailResponse.ProjectMemberInfo buildProjectMemberInfo(ProjectMember projectMember) {
        // Build skills list
        List<ProjectDetailResponse.SkillInfo> skills = projectMember.getMember().getSkills().stream()
                .map(skill -> ProjectDetailResponse.SkillInfo.builder()
                        .id(skill.getId())
                        .name(skill.getName())
                        .build())
                .collect(Collectors.toList());

        return ProjectDetailResponse.ProjectMemberInfo.builder()
                .memberId(projectMember.getMember().getId())
                .fullName(projectMember.getMember().getFullName())
                .email(projectMember.getMember().getEmail())
                .position(projectMember.getMember().getPosition() != null ?
                        projectMember.getMember().getPosition().getName() : null)
                .projectRole(projectMember.getProjectRole().getName())
                .assignedAt(projectMember.getAssignedAt())
                .unassignedAt(projectMember.getUnassignedAt())
                .active(projectMember.getUnassignedAt() == null)
                .skills(skills)
                .build();
    }
}

