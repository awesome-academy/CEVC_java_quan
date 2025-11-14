package com.example.member_management_system.service;

import com.example.member_management_system.dto.PositionDTO;
import com.example.member_management_system.entity.Position;
import com.example.member_management_system.exception.ResourceNotFoundException;
import com.example.member_management_system.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;
    private final ModelMapper modelMapper;
    private final ActivityLogService activityLogService;
    private final MessageSource messageSource;

    public Page<Position> findAll(Pageable pageable) {
        return positionRepository.findAll(pageable);
    }

    public Position findById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with ID: " + id));
    }

    @Transactional
    public Position createPosition(PositionDTO positionDTO) {
        validatePositionName(positionDTO.getName(), null);

        Position position = modelMapper.map(positionDTO, Position.class);
        Position savedPosition = positionRepository.save(position);

        logActivity("CREATE", "Create new position: " + savedPosition.getName(),
                "positions", savedPosition.getId());

        return savedPosition;
    }

    @Transactional
    public Position updatePosition(Long id, PositionDTO positionDTO) {
        Position existingPosition = findById(id);

        validatePositionName(positionDTO.getName(), id);

        existingPosition.setName(positionDTO.getName());
        existingPosition.setAbbreviation(positionDTO.getAbbreviation());

        Position updatedPosition = positionRepository.save(existingPosition);

        logActivity("UPDATE", "Update position: " + updatedPosition.getName(),
                "positions", updatedPosition.getId());

        return updatedPosition;
    }

    @Transactional
    public void deletePosition(Long id) {
        Position position = findById(id);

        positionRepository.delete(position);

        logActivity("DELETE", "Delete position: " + position.getName(),
                "positions", position.getId());
    }

    private void validatePositionName(String name, Long currentId) {
        Optional<Position> existing = positionRepository.findByNameIgnoreCase(name);
        if (existing.isPresent() && (currentId == null || !existing.get().getId().equals(currentId))) {
            String errorMessage = messageSource.getMessage(
                    "admin.positions.form.error.duplicate",
                    new Object[]{name},
                    LocaleContextHolder.getLocale()
            );
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void logActivity(String action, String description, String targetTable, Long targetId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (authentication != null) ? authentication.getName() : "SYSTEM";

        activityLogService.logActivity(email, action, description, targetTable, targetId);
    }
}
