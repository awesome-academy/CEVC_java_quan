package com.example.member_management_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectMemberAssignmentDTO {
    @NotNull(message = "{validation.projectmember.member.notnull}")
    private Long memberId;

    @NotNull(message = "{validation.projectmember.role.notnull}")
    private Long projectRoleId;
}
