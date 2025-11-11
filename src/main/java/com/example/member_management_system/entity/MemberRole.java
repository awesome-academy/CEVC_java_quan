package com.example.member_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(MemberRoleId.class)
public class MemberRole {

    @Id
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
