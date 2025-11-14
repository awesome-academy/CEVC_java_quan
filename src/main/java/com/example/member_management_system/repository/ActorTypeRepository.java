package com.example.member_management_system.repository;

import com.example.member_management_system.entity.ActorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActorTypeRepository extends JpaRepository<ActorType, Long> {
    Optional<ActorType> findByCode(String code);
}
