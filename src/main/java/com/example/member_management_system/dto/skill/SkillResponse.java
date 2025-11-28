package com.example.member_management_system.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Skill information response")
public class SkillResponse {
    @Schema(description = "Skill ID", example = "1")
    private Long id;
    @Schema(description = "Skill name", example = "Java")
    private String name;
    @Schema(description = "Skill level", example = "Advanced")
    private String level;
    @Schema(description = "Years of experience using this skill", example = "3")
    private Integer usedYears;
    @Schema(description = "Number of members who have this skill", example = "15")
    private Long memberCount;
    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;
    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
}
