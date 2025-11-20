package com.example.member_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeamDTO {
    private Long id;

    @NotBlank(message = "{validation.team.name.notblank}")
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "{validation.team.leader.notnull}")
    private Long leaderId;

    // (Optional) For display purposes in list
    private String leaderName;
    private int memberCount;
}
