package com.example.member_management_system.dto.team;

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
@Schema(description = "Team member information")
public class TeamMemberResponse {

    @Schema(description = "Member ID", example = "1")
    private Long memberId;

    @Schema(description = "Member full name", example = "Jane Smith")
    private String fullName;

    @Schema(description = "Member email", example = "jane.smith@example.com")
    private String email;

    @Schema(description = "Position information")
    private PositionInfo position;

    @Schema(description = "Team role", example = "Developer")
    private String teamRole;

    @Schema(description = "Date when member joined the team", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "Date when member left the team (null if still active)", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "Whether the member is currently active in the team", example = "true")
    private boolean current;

    @Schema(description = "List of member's skills")
    private List<SkillInfo> skills;

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
}

