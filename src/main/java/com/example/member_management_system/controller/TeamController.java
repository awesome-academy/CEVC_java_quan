package com.example.member_management_system.controller;

import com.example.member_management_system.dto.TeamDTO;
import com.example.member_management_system.dto.TeamMemberAssignmentDTO;
import com.example.member_management_system.entity.Member;
import com.example.member_management_system.entity.Team;
import com.example.member_management_system.repository.MemberRepository;
import com.example.member_management_system.repository.TeamRoleRepository;
import com.example.member_management_system.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final MemberRepository memberRepository;
    private final TeamRoleRepository teamRoleRepository;
    private final ModelMapper modelMapper;

    @GetMapping
    public String listTeams(Model model,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("name"));
        Page<Team> teamPage = teamService.findAll(pageable);
        model.addAttribute("teamPage", teamPage);

        return "admin/teams/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("teamDTO", new TeamDTO());
        model.addAttribute("members", getFreeMembers());

        return "admin/teams/form";
    }

    @PostMapping("/save")
    public String saveTeam(@Valid @ModelAttribute("teamDTO") TeamDTO teamDTO,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("members", getFreeMembers());

            return "admin/teams/form";
        }
        try {
            teamService.createTeam(teamDTO);
            redirectAttributes.addFlashAttribute("successMessageKey", "team.create.success");
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            model.addAttribute("members", getFreeMembers());

            return "admin/teams/form";
        }

        return "redirect:/admin/teams";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Team team = teamService.findById(id);
        TeamDTO teamDTO = modelMapper.map(team, TeamDTO.class);
        if (team.getLeader() != null) {
            teamDTO.setLeaderId(team.getLeader().getId());
        }

        model.addAttribute("teamDTO", teamDTO);

        List<Member> availableLeaders = getFreeMembers();
        if (!availableLeaders.contains(team.getLeader())) {
            availableLeaders.add(team.getLeader());
        }
        model.addAttribute("members", availableLeaders);

        return "admin/teams/form";
    }

    @PostMapping("/update/{id}")
    public String updateTeam(@PathVariable Long id,
                             @Valid @ModelAttribute("teamDTO") TeamDTO teamDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("members", getFreeMembers());

            return "admin/teams/form";
        }
        try {
            teamService.updateTeam(id, teamDTO);
            redirectAttributes.addFlashAttribute("successMessageKey", "team.update.success");
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            model.addAttribute("members", getFreeMembers());

            return "admin/teams/form";
        }

        return "redirect:/admin/teams";
    }

    @PostMapping("/delete/{id}")
    public String deleteTeam(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        teamService.deleteTeam(id);
        redirectAttributes.addFlashAttribute("successMessageKey", "team.delete.success");

        return "redirect:/admin/teams";
    }

    /**
     * VIEW DETAIL & MANAGE MEMBERS
     */
    @GetMapping("/{id}")
    public String viewTeamDetail(@PathVariable Long id, Model model) {
        Team team = teamService.findById(id);
        model.addAttribute("team", team);

        model.addAttribute("assignmentDTO", new TeamMemberAssignmentDTO());
        model.addAttribute("availableMembers", getFreeMembers());
        model.addAttribute("teamRoles", teamRoleRepository.findAll());

        return "admin/teams/detail";
    }

    @PostMapping("/{id}/members/add")
    public String addMemberToTeam(@PathVariable Long id,
                                  @Valid @ModelAttribute("assignmentDTO") TeamMemberAssignmentDTO assignmentDTO,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessageKey", "team.member.add.error.validation");

            return "redirect:/admin/teams/" + id;
        }

        try {
            teamService.addMemberToTeam(id, assignmentDTO);
            redirectAttributes.addFlashAttribute("successMessageKey", "team.member.add.success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/teams/" + id;
    }

    @PostMapping("/{id}/members/remove")
    public String removeMemberFromTeam(@PathVariable Long id, @RequestParam("memberId") Long memberId, RedirectAttributes redirectAttributes) {
        try {
            teamService.removeMemberFromTeam(id, memberId);
            redirectAttributes.addFlashAttribute("successMessageKey", "team.member.remove.success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/teams/" + id;
    }

    private List<Member> getFreeMembers() {
        return memberRepository.findMembersWithoutActiveTeam();
    }
}
