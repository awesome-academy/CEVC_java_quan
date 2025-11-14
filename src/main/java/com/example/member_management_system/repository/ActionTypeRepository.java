package com.example.member_management_system.repository;

import com.example.member_management_system.entity.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActionTypeRepository extends JpaRepository<ActionType, Long> {
    Optional<ActionType> findByCode(String code);
}
