package com.example.member_management_system.controller;

import com.example.member_management_system.dto.PositionDTO;
import com.example.member_management_system.entity.Position;
import com.example.member_management_system.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    /**
     * Display position list with pagination
     */
    @GetMapping
    public String listPositions(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("name").ascending());
        Page<Position> positionPage = positionService.findAll(pageable);

        model.addAttribute("positionPage", positionPage);

        return "admin/positions/list";
    }

    /**
     * Display create form
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("positionDTO", new PositionDTO());

        return "admin/positions/form";
    }

    /**
     * Handle create form submission
     */
    @PostMapping("/save")
    public String savePosition(@Valid @ModelAttribute("positionDTO") PositionDTO positionDTO,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (bindingResult.hasErrors()) {
            return "admin/positions/form";
        }

        try {
            positionService.createPosition(positionDTO);

            redirectAttributes.addFlashAttribute("successMessageKey", "position.create.success");

            return "redirect:/admin/positions";

        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());

            return "admin/positions/form";
        }
    }

    /**
     * Display edit form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Position position = positionService.findById(id);

        PositionDTO positionDTO = new PositionDTO();
        positionDTO.setId(position.getId());
        positionDTO.setName(position.getName());
        positionDTO.setAbbreviation(position.getAbbreviation());

        model.addAttribute("positionDTO", positionDTO);

        return "admin/positions/form";
    }

    /**
     * Handle edit form submission
     */
    @PostMapping("/update/{id}")
    public String updatePosition(@PathVariable Long id,
                                 @Valid @ModelAttribute("positionDTO") PositionDTO positionDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        if (bindingResult.hasErrors()) {
            return "admin/positions/form";
        }

        try {
            positionService.updatePosition(id, positionDTO);
            redirectAttributes.addFlashAttribute("successMessageKey", "position.update.success");

            return "redirect:/admin/positions";

        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());

            return "admin/positions/form";
        }
    }

    /**
     * Handle delete position
     */
    @PostMapping("/delete/{id}")
    public String deletePosition(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            positionService.deletePosition(id);

            redirectAttributes.addFlashAttribute("successMessageKey", "position.delete.success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessageKey", "position.delete.error");
        }

        return "redirect:/admin/positions";
    }
}
