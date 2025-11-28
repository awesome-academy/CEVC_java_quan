package com.example.member_management_system.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Paginated skills response")
public class SkillsPageResponse {
    @Schema(description = "List of skills")
    private List<SkillResponse> skills;
    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int currentPage;
    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;
    @Schema(description = "Total number of skills", example = "50")
    private long totalElements;
    @Schema(description = "Number of items per page", example = "10")
    private int pageSize;
    @Schema(description = "Whether this is the first page")
    private boolean first;
    @Schema(description = "Whether this is the last page")
    private boolean last;
}
