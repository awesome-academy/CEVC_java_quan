package com.example.member_management_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeamMemberAssignmentDTO {
    @NotNull(message = "{validation.teammember.member.notnull}")
    private Long memberId;

    @NotNull(message = "{validation.teammember.role.notnull}")
    private Long teamRoleId;

    private Long teamId;
}
