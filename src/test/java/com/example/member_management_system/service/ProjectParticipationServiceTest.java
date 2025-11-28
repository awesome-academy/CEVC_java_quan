package com.example.member_management_system.service;

import com.example.member_management_system.dto.project.ProjectDetailResponse;
import com.example.member_management_system.dto.project.UserProjectResponse;
import com.example.member_management_system.entity.*;
import com.example.member_management_system.exception.ResourceNotFoundException;
import com.example.member_management_system.repository.ProjectMemberRepository;
import com.example.member_management_system.repository.TeamMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectParticipationServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private ProjectParticipationService projectParticipationService;

    private Member testMember;
    private Project testProject;
    private ProjectMember testProjectMember;
    private Team testTeam;
    private Position testPosition;
    private ProjectRole testProjectRole;
    private Skill testSkill;

    @BeforeEach
    void setUp() {
        // Setup test data
        testPosition = new Position();
        testPosition.setId(1L);
        testPosition.setName("Senior Developer");
        testPosition.setAbbreviation("SD");

        testSkill = new Skill();
        testSkill.setId(1L);
        testSkill.setName("Java");

        testMember = new Member();
        testMember.setId(1L);
        testMember.setEmail("test@example.com");
        testMember.setFullName("Test User");
        testMember.setPosition(testPosition);
        testMember.setSkills(new HashSet<>(Collections.singletonList(testSkill)));

        Member leader = new Member();
        leader.setId(2L);
        leader.setEmail("leader@example.com");
        leader.setFullName("Project Leader");
        leader.setPosition(testPosition);

        testTeam = new Team();
        testTeam.setId(1L);
        testTeam.setName("Backend Team");
        testTeam.setDescription("Backend development team");

        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Member Management System");
        testProject.setAbbreviation("MMS");
        testProject.setStartDate(LocalDate.of(2024, 1, 1));
        testProject.setEndDate(LocalDate.of(2024, 12, 31));
        testProject.setStatus(1);
        testProject.setLeader(leader);
        testProject.setTeam(testTeam);

        testProjectRole = new ProjectRole();
        testProjectRole.setId(1L);
        testProjectRole.setName("Developer");

        testProjectMember = new ProjectMember();
        testProjectMember.setId(1L);
        testProjectMember.setProject(testProject);
        testProjectMember.setMember(testMember);
        testProjectMember.setProjectRole(testProjectRole);
        testProjectMember.setAssignedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
        testProjectMember.setUnassignedAt(null);
    }

    @Test
    void getUserProjects_Success() {
        // Given
        when(projectMemberRepository.findActiveProjectsByMemberEmail(anyString()))
                .thenReturn(Collections.singletonList(testProjectMember));

        // Mock batch member count query
        Object[] countResult = new Object[]{1L, 3L}; // projectId, count
        when(projectMemberRepository.countActiveMembersByProjectIds(anyList()))
                .thenReturn(Collections.singletonList(countResult));

        // When
        List<UserProjectResponse> responses = projectParticipationService.getUserProjects("test@example.com");

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);

        UserProjectResponse response = responses.get(0);
        assertThat(response.getProjectId()).isEqualTo(1L);
        assertThat(response.getProjectName()).isEqualTo("Member Management System");
        assertThat(response.getProjectAbbreviation()).isEqualTo("MMS");
        assertThat(response.getStatus()).isEqualTo(1);
        assertThat(response.getUserRole()).isEqualTo("Developer");
        assertThat(response.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(response.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(response.getAssignedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30));
        assertThat(response.getMemberCount()).isEqualTo(3);
        assertThat(response.getTeam()).isNotNull();
        assertThat(response.getTeam().getId()).isEqualTo(1L);
        assertThat(response.getTeam().getName()).isEqualTo("Backend Team");

        verify(projectMemberRepository).findActiveProjectsByMemberEmail("test@example.com");
        verify(projectMemberRepository).countActiveMembersByProjectIds(anyList());
    }

    @Test
    void getUserProjects_EmptyList_Success() {
        // Given
        when(projectMemberRepository.findActiveProjectsByMemberEmail(anyString()))
                .thenReturn(Collections.emptyList());

        // When
        List<UserProjectResponse> responses = projectParticipationService.getUserProjects("test@example.com");

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();

        verify(projectMemberRepository).findActiveProjectsByMemberEmail("test@example.com");
        verify(projectMemberRepository, never()).countActiveMembersByProjectIds(anyList());
    }

    @Test
    void getProjectDetail_Success() {
        // Given
        when(projectMemberRepository.findProjectMembershipByEmailAndProjectId(anyString(), anyLong()))
                .thenReturn(Optional.of(testProjectMember));
        when(projectMemberRepository.findProjectMembersWithDetails(anyLong()))
                .thenReturn(Collections.singletonList(testProjectMember));
        when(teamMemberRepository.findByTeamIdAndIsCurrentTrue(anyLong()))
                .thenReturn(Arrays.asList(new TeamMember(), new TeamMember(), new TeamMember()));

        // When
        ProjectDetailResponse response = projectParticipationService.getProjectDetail(1L, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isEqualTo(1L);
        assertThat(response.getProjectName()).isEqualTo("Member Management System");
        assertThat(response.getProjectAbbreviation()).isEqualTo("MMS");
        assertThat(response.getStatus()).isEqualTo(1);
        assertThat(response.getUserRole()).isEqualTo("Developer");
        assertThat(response.getUserAssignedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30));

        // Verify leader
        assertThat(response.getLeader()).isNotNull();
        assertThat(response.getLeader().getId()).isEqualTo(2L);
        assertThat(response.getLeader().getFullName()).isEqualTo("Project Leader");

        // Verify team
        assertThat(response.getTeam()).isNotNull();
        assertThat(response.getTeam().getId()).isEqualTo(1L);
        assertThat(response.getTeam().getName()).isEqualTo("Backend Team");
        assertThat(response.getTeam().getMemberCount()).isEqualTo(3);

        // Verify members
        assertThat(response.getMembers()).isNotNull();
        assertThat(response.getMembers()).hasSize(1);

        ProjectDetailResponse.ProjectMemberInfo memberInfo = response.getMembers().get(0);
        assertThat(memberInfo.getMemberId()).isEqualTo(1L);
        assertThat(memberInfo.getFullName()).isEqualTo("Test User");
        assertThat(memberInfo.getEmail()).isEqualTo("test@example.com");
        assertThat(memberInfo.getPosition()).isEqualTo("Senior Developer");
        assertThat(memberInfo.getProjectRole()).isEqualTo("Developer");
        assertThat(memberInfo.isActive()).isTrue();
        assertThat(memberInfo.getSkills()).hasSize(1);
        assertThat(memberInfo.getSkills().get(0).getName()).isEqualTo("Java");

        verify(projectMemberRepository).findProjectMembershipByEmailAndProjectId("test@example.com", 1L);
        verify(projectMemberRepository).findProjectMembersWithDetails(1L);
        verify(teamMemberRepository).findByTeamIdAndIsCurrentTrue(1L);
        verify(activityLogService).logActivity("test@example.com", "VIEW",
                "User viewed project details: Member Management System", "projects", 1L);
    }

    @Test
    void getProjectDetail_UserNotMember_ThrowsException() {
        // Given
        when(projectMemberRepository.findProjectMembershipByEmailAndProjectId(anyString(), anyLong()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectParticipationService.getProjectDetail(1L, "test@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found or you are not a member of this project");

        verify(projectMemberRepository).findProjectMembershipByEmailAndProjectId("test@example.com", 1L);
        verify(projectMemberRepository, never()).findProjectMembersWithDetails(anyLong());
        verify(activityLogService, never()).logActivity(anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    void getProjectDetail_WithNullLeader_Success() {
        // Given
        testProject.setLeader(null);
        when(projectMemberRepository.findProjectMembershipByEmailAndProjectId(anyString(), anyLong()))
                .thenReturn(Optional.of(testProjectMember));
        when(projectMemberRepository.findProjectMembersWithDetails(anyLong()))
                .thenReturn(Collections.singletonList(testProjectMember));
        when(teamMemberRepository.findByTeamIdAndIsCurrentTrue(anyLong()))
                .thenReturn(Collections.singletonList(new TeamMember()));

        // When
        ProjectDetailResponse response = projectParticipationService.getProjectDetail(1L, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getLeader()).isNull();
    }

    @Test
    void getProjectDetail_WithNullMemberPosition_Success() {
        // Given
        testMember.setPosition(null);
        when(projectMemberRepository.findProjectMembershipByEmailAndProjectId(anyString(), anyLong()))
                .thenReturn(Optional.of(testProjectMember));
        when(projectMemberRepository.findProjectMembersWithDetails(anyLong()))
                .thenReturn(Collections.singletonList(testProjectMember));
        when(teamMemberRepository.findByTeamIdAndIsCurrentTrue(anyLong()))
                .thenReturn(Collections.singletonList(new TeamMember()));

        // When
        ProjectDetailResponse response = projectParticipationService.getProjectDetail(1L, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMembers()).hasSize(1);
        assertThat(response.getMembers().get(0).getPosition()).isNull();
    }

    @Test
    void getProjectDetail_WithEmptySkills_Success() {
        // Given
        testMember.setSkills(new HashSet<>());
        when(projectMemberRepository.findProjectMembershipByEmailAndProjectId(anyString(), anyLong()))
                .thenReturn(Optional.of(testProjectMember));
        when(projectMemberRepository.findProjectMembersWithDetails(anyLong()))
                .thenReturn(Collections.singletonList(testProjectMember));
        when(teamMemberRepository.findByTeamIdAndIsCurrentTrue(anyLong()))
                .thenReturn(Collections.singletonList(new TeamMember()));

        // When
        ProjectDetailResponse response = projectParticipationService.getProjectDetail(1L, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMembers()).hasSize(1);
        assertThat(response.getMembers().get(0).getSkills()).isEmpty();
    }

    @Test
    void getProjectDetail_WithInactiveMembers_Success() {
        // Given
        ProjectMember inactiveMember = new ProjectMember();
        inactiveMember.setId(2L);
        inactiveMember.setProject(testProject);
        inactiveMember.setMember(testMember);
        inactiveMember.setProjectRole(testProjectRole);
        inactiveMember.setAssignedAt(LocalDateTime.of(2024, 1, 1, 9, 0));
        inactiveMember.setUnassignedAt(LocalDateTime.of(2024, 6, 30, 18, 0));

        when(projectMemberRepository.findProjectMembershipByEmailAndProjectId(anyString(), anyLong()))
                .thenReturn(Optional.of(testProjectMember));
        when(projectMemberRepository.findProjectMembersWithDetails(anyLong()))
                .thenReturn(Arrays.asList(testProjectMember, inactiveMember));
        when(teamMemberRepository.findByTeamIdAndIsCurrentTrue(anyLong()))
                .thenReturn(Collections.singletonList(new TeamMember()));

        // When
        ProjectDetailResponse response = projectParticipationService.getProjectDetail(1L, "test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMembers()).hasSize(2);

        // First member is active
        assertThat(response.getMembers().get(0).isActive()).isTrue();
        assertThat(response.getMembers().get(0).getUnassignedAt()).isNull();

        // Second member is inactive
        assertThat(response.getMembers().get(1).isActive()).isFalse();
        assertThat(response.getMembers().get(1).getUnassignedAt()).isNotNull();
    }
}

