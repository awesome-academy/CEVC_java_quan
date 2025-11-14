package com.example.member_management_system.repository;

import com.example.member_management_system.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {
    Optional<Position> findByName(String name);

    boolean existsByName(String name);
}
