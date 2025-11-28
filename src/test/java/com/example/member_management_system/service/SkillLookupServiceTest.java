package com.example.member_management_system.service;

import com.example.member_management_system.dto.skill.SkillsPageResponse;
import com.example.member_management_system.entity.Skill;
import com.example.member_management_system.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkillLookupService Tests")
class SkillLookupServiceTest {
    @Mock
    private SkillRepository skillRepository;
    @InjectMocks
    private SkillLookupService skillLookupService;
    private List<Skill> mockSkills;
    private List<Map<String, Object>> mockMemberCounts;

    @BeforeEach
    void setUp() {
        mockSkills = Arrays.asList(
                createSkill(1L, "Java", "Advanced", 3),
                createSkill(2L, "JavaScript", "Intermediate", 2),
                createSkill(3L, "Python", "Beginner", 1)
        );
        mockMemberCounts = new ArrayList<>();
        mockMemberCounts.add(createMemberCountMap(1L, 10L));
        mockMemberCounts.add(createMemberCountMap(2L, 8L));
        mockMemberCounts.add(createMemberCountMap(3L, 5L));
    }

    @Test
    @DisplayName("Should successfully retrieve all skills with pagination")
    void testGetSkillsWithoutSearch() {
        Page<Skill> mockPage = new PageImpl<>(mockSkills, PageRequest.of(0, 10), mockSkills.size());
        when(skillRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(skillRepository.getMemberCountsBySkillIds(anyList())).thenReturn(mockMemberCounts);
        SkillsPageResponse response = skillLookupService.getSkills(null, 0, 10);
        assertThat(response).isNotNull();
        assertThat(response.getSkills()).hasSize(3);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
        assertThat(response.getSkills().get(0).getMemberCount()).isEqualTo(10L);
        assertThat(response.getSkills().get(1).getMemberCount()).isEqualTo(8L);
        assertThat(response.getSkills().get(2).getMemberCount()).isEqualTo(5L);
        verify(skillRepository, times(1)).findAll(any(Pageable.class));
        verify(skillRepository, times(1)).getMemberCountsBySkillIds(anyList());
    }

    @Test
    @DisplayName("Should successfully retrieve skills with search term")
    void testGetSkillsWithSearch() {
        String searchTerm = "Java";
        List<Skill> filteredSkills = Arrays.asList(
                createSkill(1L, "Java", "Advanced", 3),
                createSkill(2L, "JavaScript", "Intermediate", 2)
        );
        Page<Skill> mockPage = new PageImpl<>(filteredSkills, PageRequest.of(0, 10), filteredSkills.size());
        List<Map<String, Object>> filteredMemberCounts = Arrays.asList(
                createMemberCountMap(1L, 10L),
                createMemberCountMap(2L, 8L)
        );
        when(skillRepository.searchByName(eq(searchTerm), any(Pageable.class))).thenReturn(mockPage);
        when(skillRepository.getMemberCountsBySkillIds(anyList())).thenReturn(filteredMemberCounts);
        SkillsPageResponse response = skillLookupService.getSkills(searchTerm, 0, 10);
        assertThat(response).isNotNull();
        assertThat(response.getSkills()).hasSize(2);
        assertThat(response.getSkills().get(0).getName()).isEqualTo("Java");
        assertThat(response.getSkills().get(1).getName()).isEqualTo("JavaScript");
        verify(skillRepository, times(1)).searchByName(eq(searchTerm), any(Pageable.class));
        verify(skillRepository, times(1)).getMemberCountsBySkillIds(anyList());
        verify(skillRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should return empty response when no skills found")
    void testGetSkillsEmptyResult() {
        Page<Skill> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(skillRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);
        SkillsPageResponse response = skillLookupService.getSkills(null, 0, 10);
        assertThat(response).isNotNull();
        assertThat(response.getSkills()).isEmpty();
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
        verify(skillRepository, times(1)).findAll(any(Pageable.class));
        verify(skillRepository, never()).getMemberCountsBySkillIds(anyList());
    }

    @Test
    @DisplayName("Should handle skills with zero member count")
    void testGetSkillsWithZeroMemberCount() {
        List<Skill> skillsWithNoMembers = Arrays.asList(
                createSkill(4L, "Rust", "Beginner", 1)
        );
        Page<Skill> mockPage = new PageImpl<>(skillsWithNoMembers, PageRequest.of(0, 10), 1);
        List<Map<String, Object>> emptyMemberCounts = List.of();
        when(skillRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(skillRepository.getMemberCountsBySkillIds(anyList())).thenReturn(emptyMemberCounts);
        SkillsPageResponse response = skillLookupService.getSkills(null, 0, 10);
        assertThat(response).isNotNull();
        assertThat(response.getSkills()).hasSize(1);
        assertThat(response.getSkills().get(0).getMemberCount()).isEqualTo(0L);
        verify(skillRepository, times(1)).findAll(any(Pageable.class));
        verify(skillRepository, times(1)).getMemberCountsBySkillIds(anyList());
    }

    @Test
    @DisplayName("Should handle pagination correctly for second page")
    void testGetSkillsSecondPage() {
        Page<Skill> mockPage = new PageImpl<>(mockSkills, PageRequest.of(1, 10), 25);
        when(skillRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(skillRepository.getMemberCountsBySkillIds(anyList())).thenReturn(mockMemberCounts);
        SkillsPageResponse response = skillLookupService.getSkills(null, 1, 10);
        assertThat(response).isNotNull();
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.getTotalElements()).isEqualTo(25);
        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isFalse();
        verify(skillRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should trim search term before searching")
    void testGetSkillsWithWhitespaceSearch() {
        String searchTermWithSpaces = "  Java  ";
        String trimmedSearchTerm = "Java";
        List<Skill> filteredSkills = Arrays.asList(createSkill(1L, "Java", "Advanced", 3));
        Page<Skill> mockPage = new PageImpl<>(filteredSkills, PageRequest.of(0, 10), 1);
        List<Map<String, Object>> filteredMemberCounts = Arrays.asList(createMemberCountMap(1L, 10L));
        when(skillRepository.searchByName(eq(trimmedSearchTerm), any(Pageable.class))).thenReturn(mockPage);
        when(skillRepository.getMemberCountsBySkillIds(anyList())).thenReturn(filteredMemberCounts);
        SkillsPageResponse response = skillLookupService.getSkills(searchTermWithSpaces, 0, 10);
        assertThat(response).isNotNull();
        assertThat(response.getSkills()).hasSize(1);
        verify(skillRepository, times(1)).searchByName(eq(trimmedSearchTerm), any(Pageable.class));
    }

    @Test
    @DisplayName("Should treat empty string search as no search")
    void testGetSkillsWithEmptyStringSearch() {
        String emptySearch = "   ";
        Page<Skill> mockPage = new PageImpl<>(mockSkills, PageRequest.of(0, 10), mockSkills.size());
        when(skillRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(skillRepository.getMemberCountsBySkillIds(anyList())).thenReturn(mockMemberCounts);
        SkillsPageResponse response = skillLookupService.getSkills(emptySearch, 0, 10);
        assertThat(response).isNotNull();
        assertThat(response.getSkills()).hasSize(3);
        verify(skillRepository, times(1)).findAll(any(Pageable.class));
        verify(skillRepository, never()).searchByName(anyString(), any(Pageable.class));
    }

    private Skill createSkill(Long id, String name, String level, int usedYears) {
        Skill skill = Skill.builder()
                .name(name)
                .level(level)
                .usedYears(usedYears)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        skill.setId(id);
        return skill;
    }

    private Map<String, Object> createMemberCountMap(Long skillId, Long count) {
        Map<String, Object> map = new HashMap<>();
        map.put("skillId", skillId);
        map.put("memberCount", count);
        return map;
    }
}
