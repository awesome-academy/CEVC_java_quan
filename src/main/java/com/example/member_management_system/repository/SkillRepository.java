package com.example.member_management_system.repository;

import com.example.member_management_system.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByNameIgnoreCase(String name);

    /**
     * Search skills by name with pagination
     *
     * @param searchTerm the search term to filter skills
     * @param pageable   pagination information
     * @return page of skills matching the search term
     */
    @Query("SELECT s FROM Skill s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Skill> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Get member counts for multiple skills in a single query to avoid N+1 problem
     *
     * @param skillIds list of skill IDs
     * @return map of skill ID to member count
     */
    @Query("SELECT s.id as skillId, COUNT(DISTINCT ms.member.id) as memberCount " +
            "FROM Skill s " +
            "LEFT JOIN s.memberSkills ms " +
            "WHERE s.id IN :skillIds " +
            "GROUP BY s.id")
    List<Map<String, Object>> getMemberCountsBySkillIds(@Param("skillIds") List<Long> skillIds);
}
