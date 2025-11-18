package com.example.member_management_system.controller;

import com.example.member_management_system.dto.ImportResultDTO;
import com.example.member_management_system.dto.SkillDTO;
import com.example.member_management_system.entity.Skill;
import com.example.member_management_system.service.SkillService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/admin/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    private final MessageSource messageSource;
    private final ModelMapper modelMapper;

    /**
     * List View + Pagination
     */
    @GetMapping
    public String listSkills(Model model,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("name").ascending());
        Page<Skill> skillPage = skillService.findAll(pageable);

        model.addAttribute("skillPage", skillPage);

        return "admin/skills/list";
    }

    /**
     * Show Create Form
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("skillDTO", new SkillDTO());

        return "admin/skills/form";
    }

    /**
     * Handle Create Form
     */
    @PostMapping("/save")
    public String saveSkill(@Valid @ModelAttribute("skillDTO") SkillDTO skillDTO,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/skills/form";
        }

        try {
            skillService.createSkill(skillDTO);
            redirectAttributes.addFlashAttribute("successMessageKey", "skill.create.success");

            return "redirect:/admin/skills";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());

            return "admin/skills/form";
        }
    }

    /**
     * Show Edit Form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Skill skill = skillService.findById(id);
        SkillDTO skillDTO = modelMapper.map(skill, SkillDTO.class);

        model.addAttribute("skillDTO", skillDTO);

        return "admin/skills/form";
    }

    /**
     * Handle Edit Form
     */
    @PostMapping("/update/{id}")
    public String updateSkill(@PathVariable Long id,
                              @Valid @ModelAttribute("skillDTO") SkillDTO skillDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/skills/form";
        }

        try {
            skillService.updateSkill(id, skillDTO);
            redirectAttributes.addFlashAttribute("successMessageKey", "skill.update.success");

            return "redirect:/admin/skills";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());

            return "admin/skills/form";
        }
    }

    /**
     * Handle Delete
     */
    @PostMapping("/delete/{id}")
    public String deleteSkill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            skillService.deleteSkill(id);
            redirectAttributes.addFlashAttribute("successMessageKey", "skill.delete.success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessageKey", "skill.delete.error");
        }

        return "redirect:/admin/skills";
    }

    /**
     * Handle CSV Export
     */
    @GetMapping("/export")
    public void exportSkills(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileNameKey = messageSource.getMessage("skill.export.filename", null, LocaleContextHolder.getLocale());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileNameKey + "_" + timestamp + ".csv\"");

        skillService.exportSkillsToCsv(response.getWriter());
    }

    /**
     * Handle CSV Import
     */
    @PostMapping("/import")
    public String importSkills(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    getI18nMessage("skill.import.error", "File is empty"));

            return "redirect:/admin/skills";
        }

        try {
            ImportResultDTO result = skillService.importSkillsFromCsv(file);

            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessages", result.getErrors());
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        getI18nMessage("skill.import.success", result.getImportedCount()));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    getI18nMessage("skill.import.error", e.getMessage()));
        }

        return "redirect:/admin/skills";
    }

    private String getI18nMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
