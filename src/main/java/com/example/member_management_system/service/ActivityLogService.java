package com.example.member_management_system.service;

import com.example.member_management_system.entity.ActionType;
import com.example.member_management_system.entity.ActivityLog;
import com.example.member_management_system.entity.ActorType;
import com.example.member_management_system.entity.Member;
import com.example.member_management_system.repository.ActionTypeRepository;
import com.example.member_management_system.repository.ActivityLogRepository;
import com.example.member_management_system.repository.ActorTypeRepository;
import com.example.member_management_system.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {

    private final ActivityLogRepository logRepository;
    private final MemberRepository memberRepository;
    private final ActionTypeRepository actionTypeRepository;
    private final ActorTypeRepository actorTypeRepository;

    public void logActivity(String email, String actionCode, String description) {
        try {
            Member actor = memberRepository.findByEmailWithRoles(email)
                    .orElse(null);

            ActionType actionType = actionTypeRepository.findByCode(actionCode)
                    .orElse(null);

            if (actionType == null) {
                log.warn("ActionType not found: {}", actionCode);
                return;
            }

            ActorType actorType = null;
            if (actor != null && actor.getRoles() != null && !actor.getRoles().isEmpty()) {
                boolean isAdmin = actor.getRoles().stream()
                        .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
                String actorTypeCode = isAdmin ? "ADMIN" : "USER";
                actorType = actorTypeRepository.findByCode(actorTypeCode).orElse(null);
            }

            if (actorType == null) {
                actorType = actorTypeRepository.findByCode("USER").orElse(null);
            }

            if (actorType == null) {
                log.warn("ActorType not found");
                return;
            }

            ActivityLog logEntry = new ActivityLog();

            logEntry.setActionType(actionType);
            logEntry.setActorType(actorType);
            logEntry.setDescription(description);
            logEntry.setTargetTable(Member.class.getSimpleName());
            if (actor != null) {
                logEntry.setActor(actor);
                logEntry.setTargetId(actor.getId());
            }

            logRepository.save(logEntry);
            log.debug("Activity logged: {} - {}", actionCode, description);

        } catch (Exception e) {
            log.error("Unable to write activity log: {}", e.getMessage(), e);
        }
    }
}
