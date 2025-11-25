package com.example.member_management_system.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "action_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionType extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code; // LOGIN, LOGOUT, CREATE, UPDATE, DELETE, IMPORT, EXPORT

    private String description;

    @OneToMany(mappedBy = "actionType")
    private Set<ActivityLog> activityLogs;
}
