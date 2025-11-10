package com.example.member_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = false)
    private Member actor;

    @ManyToOne
    @JoinColumn(name = "actor_type_id", nullable = false)
    private ActorType actorType;

    @ManyToOne
    @JoinColumn(name = "action_type_id", nullable = false)
    private ActionType actionType;

    @Column(name = "target_table", nullable = false)
    private String targetTable;

    @Column(name = "target_id")
    private Long targetId;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
