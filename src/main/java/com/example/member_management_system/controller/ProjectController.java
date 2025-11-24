package com.example.member_management_system.controller;

import com.example.member_management_system.dto.ProjectDTO;
import com.example.member_management_system.dto.ProjectMemberAssignmentDTO;
import com.example.member_management_system.entity.Project;
import com.example.member_management_system.exception.BusinessException;
import com.example.member_management_system.exception.DuplicateResourceException;
import com.example.member_management_system.exception.InvalidDateRangeException;
import com.example.member_management_system.repository.MemberRepository;
import com.example.member_management_system.repository.ProjectRoleRepository;
import com.example.member_management_system.repository.TeamRepository;
import com.example.member_management_system.service.ProjectService;
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

@Controller
@RequestMapping("/admin/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ModelMapper modelMapper;

    private void populateFormAttributes(Model model) {
        model.addAttribute("teams", teamRepository.findAll(Sort.by("name")));
        model.addAttribute("members", memberRepository.findAll(Sort.by("fullName")));
    }

    @GetMapping
    public String listProjects(Model model,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Project> projectPage = projectService.findAll(pageable);
        model.addAttribute("projectPage", projectPage);

        return "admin/projects/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("projectDTO", new ProjectDTO());
        populateFormAttributes(model);

        return "admin/projects/form";
    }

    @PostMapping("/save")
    public String saveProject(@Valid @ModelAttribute("projectDTO") ProjectDTO projectDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            populateFormAttributes(model);

            return "admin/projects/form";
        }
        try {
            projectService.createProject(projectDTO);
            redirectAttributes.addFlashAttribute("successMessageKey", "project.create.success");
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            populateFormAttributes(model);

            return "admin/projects/form";
        } catch (InvalidDateRangeException e) {
            bindingResult.rejectValue("endDate", "invalid", e.getMessage());
            populateFormAttributes(model);

            return "admin/projects/form";
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateFormAttributes(model);

            return "admin/projects/form";
        }

        return "redirect:/admin/projects";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id);
        ProjectDTO projectDTO = modelMapper.map(project, ProjectDTO.class);

        if (project.getTeam() != null) projectDTO.setTeamId(project.getTeam().getId());
        if (project.getLeader() != null) projectDTO.setLeaderId(project.getLeader().getId());

        model.addAttribute("projectDTO", projectDTO);
        populateFormAttributes(model);

        return "admin/projects/form";
    }

    @PostMapping("/update/{id}")
    public String updateProject(@PathVariable Long id,
                                @Valid @ModelAttribute("projectDTO") ProjectDTO projectDTO,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            populateFormAttributes(model);

            return "admin/projects/form";
        }
        try {
            projectService.updateProject(id, projectDTO);
            redirectAttributes.addFlashAttribute("successMessageKey", "project.update.success");
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            populateFormAttributes(model);

            return "admin/projects/form";
        } catch (InvalidDateRangeException e) {
            bindingResult.rejectValue("endDate", "invalid", e.getMessage());
            populateFormAttributes(model);

            return "admin/projects/form";
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateFormAttributes(model);

            return "admin/projects/form";
        }

        return "redirect:/admin/projects";
    }

    @PostMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        projectService.deleteProject(id);
        redirectAttributes.addFlashAttribute("successMessageKey", "project.delete.success");

        return "redirect:/admin/projects";
    }

    /**
     * DETAIL & MEMBERS
     */
    @GetMapping("/{id}")
    public String viewProjectDetail(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id);
        model.addAttribute("project", project);

        model.addAttribute("assignmentDTO", new ProjectMemberAssignmentDTO());
        model.addAttribute("members", memberRepository.findAll(Sort.by("fullName")));
        model.addAttribute("projectRoles", projectRoleRepository.findAll());

        return "admin/projects/detail";
    }

    @PostMapping("/{id}/members/add")
    public String assignMember(@PathVariable Long id,
                               @Valid @ModelAttribute("assignmentDTO") ProjectMemberAssignmentDTO assignmentDTO,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessageKey", "project.member.add.error.validation");

            return "redirect:/admin/projects/" + id;
        }
        try {
            projectService.assignMember(id, assignmentDTO);
            redirectAttributes.addFlashAttribute("successMessageKey", "project.member.add.success");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/projects/" + id;
    }

    @PostMapping("/{id}/members/remove")
    public String removeMember(@PathVariable Long id, @RequestParam("memberId") Long memberId, RedirectAttributes redirectAttributes) {
        try {
            projectService.removeMember(id, memberId);
            redirectAttributes.addFlashAttribute("successMessageKey", "project.member.remove.success");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/projects/" + id;
    }
}
