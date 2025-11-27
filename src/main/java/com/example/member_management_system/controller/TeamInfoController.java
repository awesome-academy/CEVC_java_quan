package com.example.member_management_system.controller;

import com.example.member_management_system.dto.team.CurrentTeamResponse;
import com.example.member_management_system.dto.team.TeamMemberResponse;
import com.example.member_management_system.service.TeamInfoService;
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
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Team Info", description = "APIs for viewing team information")
@SecurityRequirement(name = "Bearer Authentication")
public class TeamInfoController {

    private final TeamInfoService teamInfoService;

    @GetMapping("/me")
    @Operation(
            summary = "Get current user's team",
            description = "Returns the current team that the authenticated user is assigned to, " +
                    "including team details, leader information, and member count"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved current team",
                    content = @Content(schema = @Schema(implementation = CurrentTeamResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User is not currently assigned to any team",
                    content = @Content
            )
    })
    public ResponseEntity<CurrentTeamResponse> getCurrentTeam(Authentication authentication) {
        String email = authentication.getName();
        CurrentTeamResponse team = teamInfoService.getCurrentTeam(email);
        return ResponseEntity.ok(team);
    }

    @GetMapping("/{id}/members")
    @Operation(
            summary = "Get team members",
            description = "Returns the list of all members (current and past) in the specified team, " +
                    "including their positions, skills, and team roles"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved team members",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TeamMemberResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Team not found",
                    content = @Content
            )
    })
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembers(
            @Parameter(description = "Team ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        List<TeamMemberResponse> members = teamInfoService.getTeamMembers(id);
        return ResponseEntity.ok(members);
    }
}

