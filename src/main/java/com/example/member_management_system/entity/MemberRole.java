package com.example.member_management_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_roles")
@Getter
@Setter
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberRole)) return false;
        MemberRole that = (MemberRole) o;
        return member != null && role != null &&
                member.equals(that.member) && role.equals(that.role);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
