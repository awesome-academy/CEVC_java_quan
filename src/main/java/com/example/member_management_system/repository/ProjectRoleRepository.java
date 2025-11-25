package com.example.member_management_system.repository;

import com.example.member_management_system.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {
    Optional<ProjectRole> findByName(String name);
}
