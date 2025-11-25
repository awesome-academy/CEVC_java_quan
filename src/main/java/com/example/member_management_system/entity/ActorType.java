package com.example.member_management_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "actor_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorType extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code; // ADMIN, USER, SYSTEM

    private String description;

    @OneToMany(mappedBy = "actorType")
    private Set<ActivityLog> activityLogs;
}
