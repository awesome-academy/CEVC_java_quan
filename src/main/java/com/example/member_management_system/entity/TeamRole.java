package com.example.member_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "team_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // LEADER, SUBLEADER, MEMBER

    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "teamRole")
    private Set<TeamMember> teamMembers;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
