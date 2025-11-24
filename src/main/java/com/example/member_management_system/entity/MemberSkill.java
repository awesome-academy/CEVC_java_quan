package com.example.member_management_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(MemberSkillId.class)
public class MemberSkill {

    @Id
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberSkill)) return false;
        MemberSkill that = (MemberSkill) o;
        return member != null && skill != null &&
                member.equals(that.member) && skill.equals(that.skill);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
