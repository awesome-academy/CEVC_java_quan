package com.example.member_management_system.service;

import com.example.member_management_system.dto.team.CurrentTeamResponse;
import com.example.member_management_system.dto.team.TeamMemberResponse;
import com.example.member_management_system.entity.*;
import com.example.member_management_system.exception.ResourceNotFoundException;
import com.example.member_management_system.repository.TeamMemberRepository;
import com.example.member_management_system.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamInfoServiceTest {

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamInfoService teamInfoService;

    private Member testMember;
    private Team testTeam;
    private TeamMember testTeamMember;
    private Position testPosition;
    private TeamRole testTeamRole;
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
        testMember.setSkills(new HashSet<>(Arrays.asList(testSkill)));

        Member leader = new Member();
        leader.setId(2L);
        leader.setEmail("leader@example.com");
        leader.setFullName("Team Leader");
        leader.setPosition(testPosition);

        testTeam = new Team();
        testTeam.setId(1L);
        testTeam.setName("Backend Team");
        testTeam.setDescription("Backend development team");
        testTeam.setLeader(leader);

        testTeamRole = new TeamRole();
        testTeamRole.setId(1L);
        testTeamRole.setName("Developer");

        testTeamMember = new TeamMember();
        testTeamMember.setId(1L);
        testTeamMember.setTeam(testTeam);
        testTeamMember.setMember(testMember);
        testTeamMember.setTeamRole(testTeamRole);
        testTeamMember.setStartDate(LocalDate.of(2024, 1, 1));
        testTeamMember.setCurrent(true);
    }

    @Test
    void getCurrentTeam_Success() {
        // Given
        when(teamMemberRepository.findCurrentTeamByMemberEmail(anyString()))
                .thenReturn(Optional.of(testTeamMember));
        when(teamMemberRepository.findByTeamIdAndIsCurrentTrue(anyLong()))
                .thenReturn(Arrays.asList(testTeamMember, new TeamMember(), new TeamMember()));

        // When
        CurrentTeamResponse response = teamInfoService.getCurrentTeam("test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTeamId()).isEqualTo(1L);
        assertThat(response.getTeamName()).isEqualTo("Backend Team");
        assertThat(response.getTeamDescription()).isEqualTo("Backend development team");
        assertThat(response.getUserRole()).isEqualTo("Developer");
        assertThat(response.getJoinedDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(response.getMemberCount()).isEqualTo(3);
        assertThat(response.getLeader()).isNotNull();
        assertThat(response.getLeader().getId()).isEqualTo(2L);
        assertThat(response.getLeader().getFullName()).isEqualTo("Team Leader");

        verify(teamMemberRepository).findCurrentTeamByMemberEmail("test@example.com");
        verify(teamMemberRepository).findByTeamIdAndIsCurrentTrue(1L);
    }

    @Test
    void getCurrentTeam_UserNotInTeam_ThrowsException() {
        // Given
        when(teamMemberRepository.findCurrentTeamByMemberEmail(anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamInfoService.getCurrentTeam("test@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User is not currently assigned to any team");

        verify(teamMemberRepository).findCurrentTeamByMemberEmail("test@example.com");
        verify(teamMemberRepository, never()).findByTeamIdAndIsCurrentTrue(anyLong());
    }

    @Test
    void getCurrentTeam_WithNullPosition_Success() {
        // Given
        testTeam.getLeader().setPosition(null);
        when(teamMemberRepository.findCurrentTeamByMemberEmail(anyString()))
                .thenReturn(Optional.of(testTeamMember));
        when(teamMemberRepository.findByTeamIdAndIsCurrentTrue(anyLong()))
                .thenReturn(Collections.singletonList(testTeamMember));

        // When
        CurrentTeamResponse response = teamInfoService.getCurrentTeam("test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getLeader().getPosition()).isNull();
    }

    @Test
    void getTeamMembers_Success() {
        // Given
        when(teamRepository.existsById(anyLong())).thenReturn(true);
        when(teamMemberRepository.findByTeamIdWithDetails(anyLong()))
                .thenReturn(Arrays.asList(testTeamMember));

        // When
        List<TeamMemberResponse> responses = teamInfoService.getTeamMembers(1L);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);

        TeamMemberResponse response = responses.get(0);
        assertThat(response.getMemberId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("Test User");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getTeamRole()).isEqualTo("Developer");
        assertThat(response.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(response.getEndDate()).isNull();
        assertThat(response.isCurrent()).isTrue();
        assertThat(response.getPosition()).isNotNull();
        assertThat(response.getPosition().getName()).isEqualTo("Senior Developer");
        assertThat(response.getSkills()).hasSize(1);
        assertThat(response.getSkills().get(0).getName()).isEqualTo("Java");

        verify(teamRepository).existsById(1L);
        verify(teamMemberRepository).findByTeamIdWithDetails(1L);
    }

    @Test
    void getTeamMembers_TeamNotFound_ThrowsException() {
        // Given
        when(teamRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> teamInfoService.getTeamMembers(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Team not found with ID: 1");

        verify(teamRepository).existsById(1L);
        verify(teamMemberRepository, never()).findByTeamIdWithDetails(anyLong());
    }

    @Test
    void getTeamMembers_WithNullPosition_Success() {
        // Given
        testMember.setPosition(null);
        when(teamRepository.existsById(anyLong())).thenReturn(true);
        when(teamMemberRepository.findByTeamIdWithDetails(anyLong()))
                .thenReturn(Collections.singletonList(testTeamMember));

        // When
        List<TeamMemberResponse> responses = teamInfoService.getTeamMembers(1L);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getPosition()).isNull();
    }

    @Test
    void getTeamMembers_WithEmptySkills_Success() {
        // Given
        testMember.setSkills(new HashSet<>());
        when(teamRepository.existsById(anyLong())).thenReturn(true);
        when(teamMemberRepository.findByTeamIdWithDetails(anyLong()))
                .thenReturn(Collections.singletonList(testTeamMember));

        // When
        List<TeamMemberResponse> responses = teamInfoService.getTeamMembers(1L);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSkills()).isEmpty();
    }

    @Test
    void getTeamMembers_EmptyTeam_ReturnsEmptyList() {
        // Given
        when(teamRepository.existsById(anyLong())).thenReturn(true);
        when(teamMemberRepository.findByTeamIdWithDetails(anyLong()))
                .thenReturn(Collections.emptyList());

        // When
        List<TeamMemberResponse> responses = teamInfoService.getTeamMembers(1L);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();
    }
}

