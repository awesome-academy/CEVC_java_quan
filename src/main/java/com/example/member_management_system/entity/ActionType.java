package com.example.member_management_system.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "action_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // LOGIN, LOGOUT, CREATE, UPDATE, DELETE, IMPORT, EXPORT

    private String description;

    @OneToMany(mappedBy = "actionType")
    private Set<ActivityLog> activityLogs;
}
