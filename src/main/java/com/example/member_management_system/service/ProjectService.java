package com.example.member_management_system.service;

import com.example.member_management_system.dto.ProjectDTO;
import com.example.member_management_system.dto.ProjectMemberAssignmentDTO;
import com.example.member_management_system.entity.*;
import com.example.member_management_system.exception.BusinessException;
import com.example.member_management_system.exception.DuplicateResourceException;
import com.example.member_management_system.exception.InvalidDateRangeException;
import com.example.member_management_system.exception.ResourceNotFoundException;
import com.example.member_management_system.repository.*;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ModelMapper modelMapper;
    private final ActivityLogService activityLogService;
    private final MessageSource messageSource;

    public Page<Project> findAll(Pageable pageable) {
        return projectRepository.findAllWithDetails(pageable);
    }

    public Project findById(Long id) {
        return projectRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(getI18nMessage("admin.projects.error.notfound", id)));
    }

    @Transactional
    public Project createProject(ProjectDTO projectDTO) {
        validateProject(projectDTO, null);
        Project project = new Project();
        mapDtoToEntity(projectDTO, project);

        Project savedProject = projectRepository.save(project);
        logActivity("CREATE", "Created project: " + savedProject.getName(), "projects", savedProject.getId());

        return savedProject;
    }

    @Transactional
    public Project updateProject(Long id, ProjectDTO projectDTO) {
        Project existingProject = findById(id);
        validateProject(projectDTO, id);

        mapDtoToEntity(projectDTO, existingProject);

        Project updatedProject = projectRepository.save(existingProject);
        logActivity("UPDATE", "Updated project: " + updatedProject.getName(), "projects", updatedProject.getId());

        return updatedProject;
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = findById(id);

        List<ProjectMember> activeMembers = projectMemberRepository.findByProjectIdAndUnassignedAtIsNull(id);
        if (!activeMembers.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (ProjectMember pm : activeMembers) {
                pm.setUnassignedAt(now);
            }
            projectMemberRepository.saveAll(activeMembers);
        }

        projectRepository.delete(project);

        logActivity("DELETE", "Deleted project: " + project.getName(), "projects", project.getId());
    }

    @Transactional
    public void assignMember(Long projectId, ProjectMemberAssignmentDTO dto) {
        Project project = findById(projectId);
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new BusinessException(getI18nMessage("validation.projectmember.member.notfound", dto.getMemberId())));
        ProjectRole role = projectRoleRepository.findById(dto.getProjectRoleId())
                .orElseThrow(() -> new BusinessException(getI18nMessage("validation.projectmember.role.notfound", dto.getProjectRoleId())));

        Optional<ProjectMember> existing = projectMemberRepository.findActiveMemberInProject(projectId, member.getId());
        if (existing.isPresent()) {
            throw new BusinessException(getI18nMessage("admin.projects.members.error.already_exists"));
        }

        ProjectMember assignment = new ProjectMember();
        assignment.setProject(project);
        assignment.setMember(member);
        assignment.setProjectRole(role);

        projectMemberRepository.save(assignment);
        logActivity("UPDATE", "Assigned member " + member.getEmail() + " to Project " + project.getName(), "projects", projectId);
    }

    @Transactional
    public void removeMember(Long projectId, Long memberId) {
        Optional<ProjectMember> existing = projectMemberRepository.findActiveMemberInProject(projectId, memberId);
        if (existing.isPresent()) {
            ProjectMember assignment = existing.get();
            assignment.setUnassignedAt(LocalDateTime.now());
            projectMemberRepository.save(assignment);

            logActivity("UPDATE", "Removed member " + memberId + " from Project " + projectId, "projects", projectId);
        } else {
            throw new BusinessException(getI18nMessage("admin.projects.members.error.not_in_project"));
        }
    }

    private void validateProject(ProjectDTO dto, Long currentId) {
        Optional<Project> existing = projectRepository.findByNameIgnoreCase(dto.getName());
        if (existing.isPresent() && (currentId == null || !existing.get().getId().equals(currentId))) {
            throw new DuplicateResourceException(
                    getI18nMessage("admin.projects.form.error.duplicate", dto.getName()),
                    "name",
                    dto.getName()
            );
        }

        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new InvalidDateRangeException(
                    getI18nMessage("admin.projects.form.error.dates"),
                    "startDate",
                    "endDate"
            );
        }
    }

    private void mapDtoToEntity(ProjectDTO dto, Project entity) {
        entity.setName(dto.getName());
        entity.setAbbreviation(dto.getAbbreviation());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setStatus(dto.getStatus());

        Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new BusinessException(getI18nMessage("validation.project.team.notfound")));
        entity.setTeam(team);

        if (dto.getLeaderId() != null) {
            Member leader = memberRepository.findById(dto.getLeaderId())
                    .orElseThrow(() -> new BusinessException(getI18nMessage("validation.project.leader.notfound")));
            entity.setLeader(leader);
        } else {
            entity.setLeader(null);
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
