package com.example.member_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "project_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // DEV, QA, PM, BA, DESIGNER

    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "projectRole")
    private Set<ProjectMember> projectMembers;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

