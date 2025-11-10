package com.example.member_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_skills")
@Data
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
}
