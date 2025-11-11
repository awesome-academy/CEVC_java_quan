package com.example.member_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "actor_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // ADMIN, USER, SYSTEM

    private String description;

    @OneToMany(mappedBy = "actorType")
    private Set<ActivityLog> activityLogs;
}
