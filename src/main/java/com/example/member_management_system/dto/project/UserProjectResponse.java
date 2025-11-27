package com.example.member_management_system.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User's project participation summary")
public class UserProjectResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project name", example = "Member Management System")
    private String projectName;

    @Schema(description = "Project abbreviation", example = "MMS")
    private String projectAbbreviation;

    @Schema(description = "Project start date", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "Project end date (null if ongoing)", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "Project status (1: active, 0: inactive)", example = "1")
    private int status;

    @Schema(description = "User's role in the project", example = "Developer")
    private String userRole;

    @Schema(description = "Date when user was assigned to project", example = "2024-01-15T10:30:00")
    private LocalDateTime assignedAt;

    @Schema(description = "Team information")
    private TeamInfo team;

    @Schema(description = "Total number of members in the project", example = "8")
    private int memberCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Team information")
    public static class TeamInfo {
        @Schema(description = "Team ID", example = "1")
        private Long id;

        @Schema(description = "Team name", example = "Backend Team")
        private String name;
    }
}

