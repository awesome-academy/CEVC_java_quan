package com.example.member_management_system.controller;

import com.example.member_management_system.dto.skill.SkillsPageResponse;
import com.example.member_management_system.service.SkillLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@Tag(name = "Skills Lookup", description = "APIs for skills lookup and search")
@SecurityRequirement(name = "Bearer Authentication")
public class SkillLookupController {

    private final SkillLookupService skillLookupService;

    @GetMapping
    @Operation(
            summary = "Get all skills with pagination and search",
            description = "Returns a paginated list of skills. Supports searching by skill name. " +
                    "Results include member count for each skill. " +
                    "This endpoint is useful for autocomplete, profile updates, and skill browsing."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved skills list",
                    content = @Content(schema = @Schema(implementation = SkillsPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters (e.g., negative page number)",
                    content = @Content
            )
    })
    public ResponseEntity<SkillsPageResponse> getSkills(
            @Parameter(description = "Search term to filter skills by name", example = "Java")
            @RequestParam(required = false) String search,

            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        // Validate pagination parameters
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        SkillsPageResponse response = skillLookupService.getSkills(search, page, size);
        return ResponseEntity.ok(response);
    }
}

