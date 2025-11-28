package com.example.member_management_system.controller;

import com.example.member_management_system.dto.project.ProjectDetailResponse;
import com.example.member_management_system.dto.project.UserProjectResponse;
import com.example.member_management_system.service.ProjectParticipationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project Participation", description = "APIs for viewing project participation and details")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectParticipationController {

    private final ProjectParticipationService projectParticipationService;

    @GetMapping("/me")
    @Operation(
            summary = "Get user's participating projects",
            description = "Returns the list of all active projects that the authenticated user is currently participating in, " +
                    "including project details, team information, user's role, and member count"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user's projects",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserProjectResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            )
    })
    public ResponseEntity<List<UserProjectResponse>> getUserProjects(Authentication authentication) {
        String email = authentication.getName();
        List<UserProjectResponse> projects = projectParticipationService.getUserProjects(email);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get project details",
            description = "Returns detailed information about a specific project including team, leader, " +
                    "all members with their roles and skills, and the current user's role in the project. " +
                    "User must be a member of the project to view its details."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved project details",
                    content = @Content(schema = @Schema(implementation = ProjectDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found or user is not a member of this project",
                    content = @Content
            )
    })
    public ResponseEntity<ProjectDetailResponse> getProjectDetail(
            @Parameter(description = "Project ID", required = true, example = "1")
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        ProjectDetailResponse projectDetail = projectParticipationService.getProjectDetail(id, email);
        return ResponseEntity.ok(projectDetail);
    }
}

