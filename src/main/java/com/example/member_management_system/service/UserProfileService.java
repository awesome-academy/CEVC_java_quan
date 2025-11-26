package com.example.member_management_system.service;

import com.example.member_management_system.dto.user.UpdateUserProfileRequest;
import com.example.member_management_system.dto.user.UserProfileResponse;
import com.example.member_management_system.entity.Member;
import com.example.member_management_system.entity.Position;
import com.example.member_management_system.entity.Skill;
import com.example.member_management_system.entity.TeamMember;
import com.example.member_management_system.exception.ResourceNotFoundException;
import com.example.member_management_system.repository.MemberRepository;
import com.example.member_management_system.repository.PositionRepository;
import com.example.member_management_system.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final MemberRepository memberRepository;
    private final PositionRepository positionRepository;
    private final SkillRepository skillRepository;
    private final ActivityLogService activityLogService;

    /**
     * Get current user profile with full information
     *
     * @param email Current user's email from JWT
     * @return UserProfileResponse with user info, team, skills, and position
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) {
        // Fetch member with all relationships in a single query
        Member member = memberRepository.findByEmailWithAllDetails(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildUserProfileResponse(member);
    }

    /**
     * Build UserProfileResponse from Member entity
     * Helper method to avoid code duplication and redundant queries
     *
     * @param member Member entity with all relationships loaded
     * @return UserProfileResponse
     */
    private UserProfileResponse buildUserProfileResponse(Member member) {
        // Build position info
        UserProfileResponse.PositionInfo positionInfo = null;
        if (member.getPosition() != null) {
            positionInfo = UserProfileResponse.PositionInfo.builder()
                    .id(member.getPosition().getId())
                    .name(member.getPosition().getName())
                    .abbreviation(member.getPosition().getAbbreviation())
                    .build();
        }

        // Build skills list
        List<UserProfileResponse.SkillInfo> skillInfoList = new ArrayList<>();
        if (member.getSkills() != null && !member.getSkills().isEmpty()) {
            skillInfoList = member.getSkills().stream()
                    .map(skill -> UserProfileResponse.SkillInfo.builder()
                            .id(skill.getId())
                            .name(skill.getName())
                            .build())
                    .collect(Collectors.toList());
        }

        // Build current teams list
        List<UserProfileResponse.TeamInfo> teamInfoList = new ArrayList<>();
        if (member.getTeamMembers() != null && !member.getTeamMembers().isEmpty()) {
            teamInfoList = member.getTeamMembers().stream()
                    .filter(TeamMember::isCurrent)
                    .map(tm -> UserProfileResponse.TeamInfo.builder()
                            .id(tm.getTeam().getId())
                            .name(tm.getTeam().getName())
                            .role(tm.getTeamRole() != null ? tm.getTeamRole().getName() : null)
                            .startDate(tm.getStartDate())
                            .build())
                    .collect(Collectors.toList());
        }

        return UserProfileResponse.builder()
                .id(member.getId())
                .fullName(member.getFullName())
                .email(member.getEmail())
                .birthday(member.getBirthday())
                .active(member.isActive())
                .position(positionInfo)
                .skills(skillInfoList)
                .currentTeams(teamInfoList)
                .build();
    }

    /**
     * Update current user profile
     *
     * @param email   Current user's email from JWT
     * @param request Update profile request
     * @return Updated UserProfileResponse
     */
    @Transactional
    public UserProfileResponse updateUserProfile(String email, UpdateUserProfileRequest request) {
        // Fetch member with all relationships in a single query
        Member member = memberRepository.findByEmailWithAllDetails(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update basic information
        member.setFullName(request.getFullName());
        member.setBirthday(request.getBirthday());

        // Update position
        if (request.getPositionId() != null) {
            Position position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Position not found with ID: " + request.getPositionId()));
            member.setPosition(position);
        }

        // Update skills - use batch query to avoid N+1 problem
        // Handle both null (no change) and empty list (clear all skills)
        if (request.getSkillIds() != null) {
            if (request.getSkillIds().isEmpty()) {
                // Clear all skills if empty list is provided
                member.setSkills(new HashSet<>());
            } else {
                // Fetch all skills in a single batch query
                Set<Skill> skills = new HashSet<>(skillRepository.findAllById(request.getSkillIds()));

                // Validate that all requested skills exist
                if (skills.size() != request.getSkillIds().size()) {
                    throw new ResourceNotFoundException("One or more skills not found");
                }

                member.setSkills(skills);
            }
        }

        Member updatedMember = memberRepository.save(member);

        // Log activity
        activityLogService.logActivity(
                email,
                "UPDATE",
                "User updated their profile",
                "members",
                updatedMember.getId()
        );

        log.info("User profile updated successfully for email: {}", email);

        // Build response directly from updatedMember to avoid redundant query
        return buildUserProfileResponse(updatedMember);
    }
}

