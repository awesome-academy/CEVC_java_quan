package com.example.member_management_system.controller;

import com.example.member_management_system.dto.MemberDTO;
import com.example.member_management_system.entity.Member;
import com.example.member_management_system.repository.PositionRepository;
import com.example.member_management_system.repository.RoleRepository;
import com.example.member_management_system.repository.SkillRepository;
import com.example.member_management_system.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PositionRepository positionRepository;
    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final MessageSource messageSource;
    private final ModelMapper modelMapper;

    // Populate common data for all views
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("positions", positionRepository.findAll(Sort.by("name")));
        model.addAttribute("allRoles", roleRepository.findAll());
        model.addAttribute("allSkills", skillRepository.findAll(Sort.by("name")));
    }

    @GetMapping
    public String listMembers(Model model,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Member> memberPage = memberService.findAll(pageable);
        model.addAttribute("memberPage", memberPage);

        return "admin/members/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("memberDTO", new MemberDTO());

        return "admin/members/form";
    }

    @PostMapping("/save")
    public String saveMember(@Validated({jakarta.validation.groups.Default.class, MemberDTO.OnCreate.class})
                             @ModelAttribute("memberDTO") MemberDTO memberDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/members/form";
        }
        try {
            memberService.createMember(memberDTO);
            redirectAttributes.addFlashAttribute("successMessageKey", "member.create.success");
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("email", "duplicate", e.getMessage());

            return "admin/members/form";
        }

        return "redirect:/admin/members";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Member member = memberService.findById(id);
        MemberDTO memberDTO = modelMapper.map(member, MemberDTO.class);

        // Manually map collections IDs for UI checkboxes
        memberDTO.setRoleIds(member.getRoles().stream().map(r -> r.getId()).collect(Collectors.toList()));
        memberDTO.setSkillIds(member.getSkills().stream().map(s -> s.getId()).collect(Collectors.toList()));
        memberDTO.setPassword(null); // Clear password for security

        model.addAttribute("memberDTO", memberDTO);

        return "admin/members/form";
    }

    @PostMapping("/update/{id}")
    public String updateMember(@PathVariable Long id,
                               @Validated(jakarta.validation.groups.Default.class)
                               @ModelAttribute("memberDTO") MemberDTO memberDTO,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/members/form";
        }
        try {
            memberService.updateMember(id, memberDTO);
            redirectAttributes.addFlashAttribute("successMessageKey", "member.update.success");
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("email", "duplicate", e.getMessage());

            return "admin/members/form";
        }

        return "redirect:/admin/members";
    }

    @PostMapping("/delete/{id}")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        memberService.deleteMember(id);
        redirectAttributes.addFlashAttribute("successMessageKey", "member.delete.success");

        return "redirect:/admin/members";
    }

    @GetMapping("/export")
    public void exportMembers(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        response.setHeader("Content-Disposition", "attachment; filename=\"members_" + timestamp + ".csv\"");
        memberService.exportMembersToCsv(response.getWriter());
    }
}
