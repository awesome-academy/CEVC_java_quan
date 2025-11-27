package com.example.member_management_system.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile response with full information")
public class UserProfileResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Birthday", example = "1990-01-15")
    private LocalDate birthday;

    @Schema(description = "Account active status", example = "true")
    private boolean active;

    @Schema(description = "Position information")
    private PositionInfo position;

    @Schema(description = "List of skills")
    private List<SkillInfo> skills;

    @Schema(description = "List of current teams")
    private List<TeamInfo> currentTeams;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Position information")
    public static class PositionInfo {
        @Schema(description = "Position ID", example = "1")
        private Long id;

        @Schema(description = "Position name", example = "Senior Developer")
        private String name;

        @Schema(description = "Position abbreviation", example = "SD")
        private String abbreviation;
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

        @Schema(description = "Team role", example = "Developer")
        private String role;

        @Schema(description = "Start date in team", example = "2024-01-01")
        private LocalDate startDate;
    }
}

