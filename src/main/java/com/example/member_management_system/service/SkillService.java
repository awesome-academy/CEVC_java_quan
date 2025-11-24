package com.example.member_management_system.service;

import com.example.member_management_system.dto.ImportResultDTO;
import com.example.member_management_system.dto.SkillDTO;
import com.example.member_management_system.entity.Skill;
import com.example.member_management_system.exception.DuplicateResourceException;
import com.example.member_management_system.exception.ResourceNotFoundException;
import com.example.member_management_system.repository.SkillRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {

    private final SkillRepository skillRepository;
    private final ModelMapper modelMapper;
    private final ActivityLogService activityLogService;
    private final MessageSource messageSource;
    private final Validator validator;

    /**
     * Get Skill list (paginated)
     */
    public Page<Skill> findAll(Pageable pageable) {
        return skillRepository.findAll(pageable);
    }

    /**
     * Get Skill by ID
     */
    public Skill findById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + id));
    }

    /**
     * Create new Skill
     */
    @Transactional
    public Skill createSkill(SkillDTO skillDTO) {
        validateSkillName(skillDTO.getName(), null);

        Skill skill = modelMapper.map(skillDTO, Skill.class);
        Skill savedSkill = skillRepository.save(skill);

        logActivity("CREATE", "Created skill: " + savedSkill.getName(), "skills", savedSkill.getId());

        return savedSkill;
    }

    /**
     * Update Skill
     */
    @Transactional
    public Skill updateSkill(Long id, SkillDTO skillDTO) {
        Skill existingSkill = findById(id);
        validateSkillName(skillDTO.getName(), id);

        existingSkill.setName(skillDTO.getName());
        existingSkill.setLevel(skillDTO.getLevel());
        existingSkill.setUsedYears(skillDTO.getUsedYears());

        Skill updatedSkill = skillRepository.save(existingSkill);
        logActivity("UPDATE", "Updated skill: " + updatedSkill.getName(), "skills", updatedSkill.getId());

        return updatedSkill;
    }

    /**
     * Delete Skill
     */
    @Transactional
    public void deleteSkill(Long id) {
        Skill skill = findById(id);
        skillRepository.delete(skill);

        logActivity("DELETE", "Deleted skill: " + skill.getName(), "skills", skill.getId());
    }

    /**
     * Export Skills to CSV
     */
    public void exportSkillsToCsv(Writer writer) throws IOException {
        List<Skill> skills = skillRepository.findAll();

        final String[] HEADERS = {"id", "name", "level", "used_years"};

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(HEADERS))) {
            for (Skill skill : skills) {
                csvPrinter.printRecord(
                        skill.getId(),
                        skill.getName(),
                        skill.getLevel(),
                        skill.getUsedYears()
                );
            }
        }
    }

    /**
     * Import Skills from CSV
     *
     * @return List of errors (if any)
     */
    @Transactional
    public ImportResultDTO importSkillsFromCsv(MultipartFile file) throws IOException {

        ImportResultDTO result = new ImportResultDTO();
        List<Skill> skillsToSave = new ArrayList<>();

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader("name", "level", "used_years")
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build();

            CSVParser csvParser = new CSVParser(fileReader, csvFormat);

            List<CSVRecord> records = csvParser.getRecords();
            int rowNum = 1;

            for (CSVRecord record : records) {
                rowNum++;
                SkillDTO dto = new SkillDTO(
                        record.get("name"),
                        record.get("level"),
                        record.get("used_years")
                );

                Set<ConstraintViolation<SkillDTO>> violations = validator.validate(dto);
                if (!violations.isEmpty()) {
                    String errorMsg = violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(", "));
                    result.getErrors().add(getI18nMessage("skill.import.validation.error", rowNum, errorMsg));
                    continue;
                }

                try {
                    validateSkillName(dto.getName(), null);
                } catch (IllegalArgumentException e) {
                    result.getErrors().add(getI18nMessage("skill.import.validation.error", rowNum, e.getMessage()));
                    continue;
                }

                skillsToSave.add(modelMapper.map(dto, Skill.class));
            }
        }

        if (!result.hasErrors() && !skillsToSave.isEmpty()) {
            skillRepository.saveAll(skillsToSave);
            logActivity("IMPORT", "Imported " + skillsToSave.size() + " skills from CSV", "skills", null);

            result.setImportedCount(skillsToSave.size());
        }

        return result;
    }

    private void validateSkillName(String name, Long currentId) {
        Optional<Skill> existing = skillRepository.findByNameIgnoreCase(name);
        if (existing.isPresent() && (currentId == null || !existing.get().getId().equals(currentId))) {
            throw new DuplicateResourceException(
                    getI18nMessage("admin.skills.form.error.duplicate", name),
                    "name",
                    name
            );
        }
    }

    private void logActivity(String action, String description, String targetTable, Long targetId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (authentication != null) ? authentication.getName() : "SYSTEM";
        activityLogService.logActivity(email, action, description, targetTable, targetId);
    }

    private String getI18nMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
