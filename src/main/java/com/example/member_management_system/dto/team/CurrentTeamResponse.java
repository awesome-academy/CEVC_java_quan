package com.example.member_management_system.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Current team information for the authenticated user")
public class CurrentTeamResponse {

    @Schema(description = "Team ID", example = "1")
    private Long teamId;

    @Schema(description = "Team name", example = "Backend Team")
    private String teamName;

    @Schema(description = "Team description", example = "Responsible for backend development")
    private String teamDescription;

    @Schema(description = "Team leader information")
    private LeaderInfo leader;

    @Schema(description = "User's role in the team", example = "Developer")
    private String userRole;

    @Schema(description = "Date when user joined the team", example = "2024-01-01")
    private LocalDate joinedDate;

    @Schema(description = "Total number of members in the team", example = "5")
    private int memberCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Team leader information")
    public static class LeaderInfo {
        @Schema(description = "Leader member ID", example = "1")
        private Long id;

        @Schema(description = "Leader full name", example = "John Doe")
        private String fullName;

        @Schema(description = "Leader email", example = "john.doe@example.com")
        private String email;

        @Schema(description = "Leader position", example = "Senior Developer")
        private String position;
    }
}

