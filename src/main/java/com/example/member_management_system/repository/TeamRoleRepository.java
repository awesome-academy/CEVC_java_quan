package com.example.member_management_system.repository;

import com.example.member_management_system.entity.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRoleRepository extends JpaRepository<TeamRole, Long> {
    Optional<TeamRole> findByName(String name);
}
