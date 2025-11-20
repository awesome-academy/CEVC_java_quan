package com.example.member_management_system.repository;

import com.example.member_management_system.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("SELECT m FROM Member m JOIN FETCH m.roles WHERE LOWER(m.email) = LOWER(:email)")
    Optional<Member> findByEmailWithRoles(@Param("email") String email);

    Optional<Member> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    // Used for Edit Form - Fetch Position, Roles, and Skills to avoid LazyInit
    @Query("SELECT m FROM Member m " +
            "LEFT JOIN FETCH m.position " +
            "LEFT JOIN FETCH m.roles " +
            "LEFT JOIN FETCH m.skills " +
            "WHERE m.id = :id")
    Optional<Member> findByIdWithDetails(@Param("id") Long id);

    // Used for List View - Fetch Position to avoid N+1
    // (Note: We don't fetch roles/skills here for performance, unless needed in table)
    @Query(value = "SELECT m FROM Member m JOIN FETCH m.position",
            countQuery = "SELECT COUNT(m) FROM Member m")
    Page<Member> findAllWithPosition(Pageable pageable);

    // For Export CSV (Fetch all relations)
    @Query("SELECT DISTINCT m FROM Member m " +
            "LEFT JOIN FETCH m.position " +
            "LEFT JOIN FETCH m.skills")
    List<Member> findAllForExport();

    @Query("SELECT m FROM Member m " +
            "WHERE NOT EXISTS (" +
            "  SELECT tm FROM TeamMember tm WHERE tm.member.id = m.id AND tm.isCurrent = true" +
            ") " +
            "ORDER BY m.fullName ASC")
    List<Member> findMembersWithoutActiveTeam();
}

