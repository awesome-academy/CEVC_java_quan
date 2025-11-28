package com.example.member_management_system.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed project information")
public class ProjectDetailResponse {

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

    @Schema(description = "Project leader information")
    private LeaderInfo leader;

    @Schema(description = "Team information")
    private TeamInfo team;

    @Schema(description = "Current user's role in the project", example = "Developer")
    private String userRole;

    @Schema(description = "Date when user was assigned to project", example = "2024-01-15T10:30:00")
    private LocalDateTime userAssignedAt;

    @Schema(description = "List of project members")
    private List<ProjectMemberInfo> members;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Project leader information")
    public static class LeaderInfo {
        @Schema(description = "Leader member ID", example = "1")
        private Long id;

        @Schema(description = "Leader full name", example = "John Doe")
        private String fullName;

        @Schema(description = "Leader email", example = "john.doe@example.com")
        private String email;

        @Schema(description = "Leader position", example = "Project Manager")
        private String position;
    }

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

        @Schema(description = "Team description", example = "Responsible for backend development")
        private String description;

        @Schema(description = "Number of members in team", example = "5")
        private int memberCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Project member information")
    public static class ProjectMemberInfo {
        @Schema(description = "Member ID", example = "2")
        private Long memberId;

        @Schema(description = "Member full name", example = "Jane Smith")
        private String fullName;

        @Schema(description = "Member email", example = "jane.smith@example.com")
        private String email;

        @Schema(description = "Member position", example = "Senior Developer")
        private String position;

        @Schema(description = "Project role", example = "Developer")
        private String projectRole;

        @Schema(description = "Date when member was assigned to project", example = "2024-01-15T10:30:00")
        private LocalDateTime assignedAt;

        @Schema(description = "Date when member was unassigned (null if still active)", example = "2024-12-31T18:00:00")
        private LocalDateTime unassignedAt;

        @Schema(description = "Whether the member is currently active in the project", example = "true")
        private boolean active;

        @Schema(description = "List of member's skills")
        private List<SkillInfo> skills;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Skill information")
    public static class SkillInfo {
        @Schema(description = "Skill ID", example = "1")
        private Long id;

        @Schema(description = "Skill name", example = "Java")
        private String name;
    }
}

