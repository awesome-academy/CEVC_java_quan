package com.example.member_management_system.service;

import com.example.member_management_system.dto.skill.SkillResponse;
import com.example.member_management_system.dto.skill.SkillsPageResponse;
import com.example.member_management_system.entity.Skill;
import com.example.member_management_system.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SkillLookupService {

    private final SkillRepository skillRepository;

    /**
     * Get all skills with pagination and optional search
     * Uses caching for better performance
     * Avoids N+1 query by fetching member counts in a single batch query
     *
     * @param searchTerm optional search term to filter skills by name
     * @param page       page number (0-indexed)
     * @param size       page size
     * @return paginated skills response
     */
    @Cacheable(value = "skills", key = "#searchTerm + '_' + #page + '_' + #size", unless = "#result == null")
    public SkillsPageResponse getSkills(String searchTerm, int page, int size) {
        log.info("Fetching skills - searchTerm: {}, page: {}, size: {}", searchTerm, page, size);

        // Create pageable with sorting by name
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        // Fetch skills based on search term
        Page<Skill> skillPage;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            skillPage = skillRepository.searchByName(searchTerm.trim(), pageable);
        } else {
            skillPage = skillRepository.findAll(pageable);
        }

        // If no skills found, return empty response
        if (skillPage.isEmpty()) {
            return buildEmptyResponse(page, size);
        }

        // Get all skill IDs from current page
        List<Long> skillIds = skillPage.getContent().stream()
                .map(Skill::getId)
                .collect(Collectors.toList());

        // Fetch member counts in a single batch query to avoid N+1 problem
        Map<Long, Long> memberCountMap = getMemberCountsMap(skillIds);

        // Map skills to response DTOs
        List<SkillResponse> skillResponses = skillPage.getContent().stream()
                .map(skill -> mapToSkillResponse(skill, memberCountMap))
                .collect(Collectors.toList());

        // Build paginated response
        return SkillsPageResponse.builder()
                .skills(skillResponses)
                .currentPage(skillPage.getNumber())
                .totalPages(skillPage.getTotalPages())
                .totalElements(skillPage.getTotalElements())
                .pageSize(skillPage.getSize())
                .first(skillPage.isFirst())
                .last(skillPage.isLast())
                .build();
    }

    /**
     * Get member counts for skills in a single batch query
     * This prevents N+1 query problem
     *
     * @param skillIds list of skill IDs
     * @return map of skill ID to member count
     */
    private Map<Long, Long> getMemberCountsMap(List<Long> skillIds) {
        List<Map<String, Object>> results = skillRepository.getMemberCountsBySkillIds(skillIds);

        Map<Long, Long> memberCountMap = new HashMap<>();
        for (Map<String, Object> result : results) {
            Long skillId = ((Number) result.get("skillId")).longValue();
            Long count = ((Number) result.get("memberCount")).longValue();
            memberCountMap.put(skillId, count);
        }

        return memberCountMap;
    }

    /**
     * Map Skill entity to SkillResponse DTO
     *
     * @param skill          skill entity
     * @param memberCountMap map of skill ID to member count
     * @return skill response DTO
     */
    private SkillResponse mapToSkillResponse(Skill skill, Map<Long, Long> memberCountMap) {
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .level(skill.getLevel())
                .usedYears(skill.getUsedYears())
                .memberCount(memberCountMap.getOrDefault(skill.getId(), 0L))
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }

    /**
     * Build empty response when no skills found
     *
     * @param page requested page number
     * @param size requested page size
     * @return empty skills page response
     */
    private SkillsPageResponse buildEmptyResponse(int page, int size) {
        return SkillsPageResponse.builder()
                .skills(List.of())
                .currentPage(page)
                .totalPages(0)
                .totalElements(0L)
                .pageSize(size)
                .first(true)
                .last(true)
                .build();
    }
}

