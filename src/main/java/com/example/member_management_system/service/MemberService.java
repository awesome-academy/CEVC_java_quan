package com.example.member_management_system.service;

import com.example.member_management_system.dto.MemberDTO;
import com.example.member_management_system.entity.Member;
import com.example.member_management_system.entity.Position;
import com.example.member_management_system.entity.Role;
import com.example.member_management_system.entity.Skill;
import com.example.member_management_system.exception.ResourceNotFoundException;
import com.example.member_management_system.repository.MemberRepository;
import com.example.member_management_system.repository.PositionRepository;
import com.example.member_management_system.repository.RoleRepository;
import com.example.member_management_system.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PositionRepository positionRepository;
    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final ActivityLogService activityLogService;
    private final MessageSource messageSource;

    public Page<Member> findAll(Pageable pageable) {
        return memberRepository.findAllWithPosition(pageable);
    }

    public Member findById(Long id) {
        return memberRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));
    }

    @Transactional
    public Member createMember(MemberDTO memberDTO) {
        Member member = new Member();

        return saveMemberCommon(member, memberDTO, "CREATE", "Created member");
    }

    @Transactional
    public Member updateMember(Long id, MemberDTO memberDTO) {
        Member existingMember = findById(id);

        return saveMemberCommon(existingMember, memberDTO, "UPDATE", "Updated member");
    }

    private Member saveMemberCommon(Member member, MemberDTO memberDTO, String action, String logDescPrefix) {
        validateEmail(memberDTO.getEmail(), member.getId());

        mapDtoToEntity(memberDTO, member);

        if (memberDTO.getPassword() != null && !memberDTO.getPassword().isBlank()) {
            member.setPassword(passwordEncoder.encode(memberDTO.getPassword()));
        }

        Member savedMember = memberRepository.save(member);

        logActivity(action, logDescPrefix + ": " + savedMember.getEmail(), "members", savedMember.getId());

        return savedMember;
    }

    @Transactional
    public void deleteMember(Long id) {
        Member member = findById(id);
        // Soft delete handled by @SQLDelete in Entity
        memberRepository.delete(member);
        logActivity("DELETE", "Deleted member: " + member.getEmail(), "members", member.getId());
    }

    /**
     * Helper to map DTO to Entity (Handling Relationships)
     */
    private void mapDtoToEntity(MemberDTO dto, Member entity) {
        // Map simple fields (fullName, email, birthday, active) using ModelMapper
        String currentHashedPassword = entity.getPassword();
        modelMapper.map(dto, entity);
        entity.setPassword(currentHashedPassword); // Preserve existing password unless changed

        // Map Position (Manual Lookup to ensure existence)
        if (dto.getPositionId() != null) {
            Position position = positionRepository.findById(dto.getPositionId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Position ID"));
            entity.setPosition(position);
        }

        // Map Roles
        if (dto.getRoleIds() != null) {
            List<Role> roles = roleRepository.findAllById(dto.getRoleIds());
            entity.setRoles(new HashSet<>(roles));
        }

        // Map Skills
        if (dto.getSkillIds() != null) {
            List<Skill> skills = skillRepository.findAllById(dto.getSkillIds());
            entity.setSkills(new HashSet<>(skills));
        }
    }

    public void exportMembersToCsv(Writer writer) throws IOException {
        List<Member> members = memberRepository.findAllForExport();
        final String[] HEADERS = {"ID", "Full Name", "Email", "Position", "Status", "Skills"};

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(HEADERS).build())) {
            for (Member m : members) {
                String skillNames = m.getSkills().stream()
                        .map(Skill::getName)
                        .collect(Collectors.joining(", "));

                csvPrinter.printRecord(
                        m.getId(),
                        m.getFullName(),
                        m.getEmail(),
                        m.getPosition().getName(),
                        m.isActive() ? "Active" : "Inactive",
                        skillNames
                );
            }
        }
    }

    private void validateEmail(String email, Long currentId) {
        Optional<Member> existing = memberRepository.findByEmailWithRoles(email); // Reuse existing query
        if (existing.isPresent() && (currentId == null || !existing.get().getId().equals(currentId))) {
            throw new IllegalArgumentException(getI18nMessage("admin.members.form.error.email.duplicate", email));
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
