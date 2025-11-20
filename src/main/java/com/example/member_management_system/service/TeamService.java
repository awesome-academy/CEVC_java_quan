package com.example.member_management_system.service;

import com.example.member_management_system.dto.TeamDTO;
import com.example.member_management_system.dto.TeamMemberAssignmentDTO;
import com.example.member_management_system.entity.Member;
import com.example.member_management_system.entity.Team;
import com.example.member_management_system.entity.TeamMember;
import com.example.member_management_system.entity.TeamRole;
import com.example.member_management_system.exception.BusinessException;
import com.example.member_management_system.exception.ResourceNotFoundException;
import com.example.member_management_system.repository.MemberRepository;
import com.example.member_management_system.repository.TeamMemberRepository;
import com.example.member_management_system.repository.TeamRepository;
import com.example.member_management_system.repository.TeamRoleRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRoleRepository teamRoleRepository;
    private final ModelMapper modelMapper;
    private final ActivityLogService activityLogService;
    private final MessageSource messageSource;

    public Page<Team> findAll(Pageable pageable) {
        return teamRepository.findAllWithLeader(pageable);
    }

    public Team findById(Long id) {
        return teamRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(getI18nMessage("admin.teams.error.notfound", id)));
    }

    @Transactional
    public Team createTeam(TeamDTO teamDTO) {
        validateTeamName(teamDTO.getName(), null);
        Team team = new Team();
        mapDtoToEntity(teamDTO, team);

        Team savedTeam = teamRepository.save(team);
        logActivity("CREATE", "Created team: " + savedTeam.getName(), "teams", savedTeam.getId());

        return savedTeam;
    }

    @Transactional
    public Team updateTeam(Long id, TeamDTO teamDTO) {
        Team existingTeam = findById(id);
        validateTeamName(teamDTO.getName(), id);
        mapDtoToEntity(teamDTO, existingTeam);

        Team updatedTeam = teamRepository.save(existingTeam);
        logActivity("UPDATE", "Updated team: " + updatedTeam.getName(), "teams", updatedTeam.getId());

        return updatedTeam;
    }

    @Transactional
    public void deleteTeam(Long id) {
        Team team = findById(id);

        List<TeamMember> activeMembers = teamMemberRepository.findByTeamIdAndIsCurrentTrue(id);

        if (!activeMembers.isEmpty()) {
            LocalDate now = LocalDate.now();
            for (TeamMember tm : activeMembers) {
                tm.setEndDate(now);
            }
            teamMemberRepository.saveAll(activeMembers);
        }

        teamRepository.delete(team);

        logActivity("DELETE", "Deleted team: " + team.getName(), "teams", team.getId());
    }

    /**
     * Add/Move a member to a team with a specific role.
     */
    @Transactional
    public void addMemberToTeam(Long teamId, TeamMemberAssignmentDTO assignmentDTO) {
        Team team = findById(teamId);
        Member member = memberRepository.findById(assignmentDTO.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException(getI18nMessage("validation.teammember.member.notfound", assignmentDTO.getMemberId())));
        TeamRole role = teamRoleRepository.findById(assignmentDTO.getTeamRoleId())
                .orElseThrow(() -> new IllegalArgumentException(getI18nMessage("validation.teammember.role.notfound", assignmentDTO.getTeamRoleId())));

        Optional<TeamMember> currentAssignment = teamMemberRepository.findCurrentByMemberId(member.getId());

        if (currentAssignment.isPresent()) {
            TeamMember current = currentAssignment.get();

            if (current.getTeam().getId().equals(teamId)) {
                if (!current.getTeamRole().getId().equals(role.getId())) {
                    closeAssignment(current);
                } else {
                    throw new BusinessException(getI18nMessage("admin.teams.members.error.already_exists"));
                }
            } else {
                closeAssignment(current);
                logActivity("UPDATE", "Moved member " + member.getEmail() + " from Team " + current.getTeam().getName(), "team_members", current.getId());
            }
        }

        TeamMember newAssignment = new TeamMember();
        newAssignment.setTeam(team);
        newAssignment.setMember(member);
        newAssignment.setTeamRole(role);
        newAssignment.setStartDate(LocalDate.now());
        newAssignment.setCurrent(true);

        teamMemberRepository.save(newAssignment);

        logActivity("UPDATE", "Added member " + member.getEmail() + " to Team " + team.getName(), "teams", team.getId());
    }

    @Transactional
    public void removeMemberFromTeam(Long teamId, Long memberId) {
        Optional<TeamMember> currentAssignment = teamMemberRepository.findCurrentByMemberId(memberId);

        if (currentAssignment.isPresent() && currentAssignment.get().getTeam().getId().equals(teamId)) {
            closeAssignment(currentAssignment.get());
            logActivity("UPDATE", "Removed member " + memberId + " from Team " + teamId, "teams", teamId);
        } else {
            throw new IllegalArgumentException(getI18nMessage("admin.teams.members.error.not_in_team"));
        }
    }

    private void closeAssignment(TeamMember assignment) {
        assignment.setEndDate(LocalDate.now());
        teamMemberRepository.save(assignment);
    }

    private void mapDtoToEntity(TeamDTO dto, Team entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());

        Member leader = memberRepository.findById(dto.getLeaderId())
                .orElseThrow(() -> new IllegalArgumentException(getI18nMessage("validation.team.leader.notfound")));
        entity.setLeader(leader);
    }

    private void validateTeamName(String name, Long currentId) {
        Optional<Team> existing = teamRepository.findByNameIgnoreCase(name);

        if (existing.isPresent() && (currentId == null || !existing.get().getId().equals(currentId))) {
            throw new IllegalArgumentException(getI18nMessage("admin.teams.form.error.duplicate", name));
        }
    }

    private void logActivity(String action, String description, String targetTable, Long targetId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (authentication != null) ? authentication.getName() : "SYSTEM";
        activityLogService.logActivity(email, action, description, targetTable, targetId);
    }

    private String getI18nMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
