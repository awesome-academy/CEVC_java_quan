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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {

    private final ActivityLogRepository logRepository;
    private final MemberRepository memberRepository;
    private final ActionTypeRepository actionTypeRepository;
    private final ActorTypeRepository actorTypeRepository;

    /**
     * === MAIN FUNCTION (5 PARAMETERS) ===
     * Used for CRUD operations and others
     * Log with full information about the affected object.
     *
     * @param email       Actor's email
     * @param actionCode  Action code (CREATE, UPDATE, DELETE, etc.)
     * @param description Description of the action
     * @param targetTable Name of the affected table (vd: "positions")
     * @param targetId    ID of the affected record
     */
    @Transactional
    public void logActivity(String email, String actionCode, String description, String targetTable, Long targetId) {
        try {
            // Get Actor (The person performing the action)
            Member actor = memberRepository.findByEmailWithRoles(email).orElse(null);

            // Get ActionType (Type of action)
            ActionType actionType = actionTypeRepository.findByCode(actionCode)
                    .orElseThrow(() -> new RuntimeException("ActionType not found: " + actionCode));

            // Get ActorType (Type of actor: ADMIN or USER)
            String actorTypeCode;
            if (actor != null && actor.getRoles() != null) {
                actorTypeCode = actor.getRoles().stream()
                        .anyMatch(r -> r.getName().equals("ROLE_ADMIN"))
                        ? "ADMIN" : "USER";
            } else {
                actorTypeCode = "USER";
            }

            ActorType actorType = actorTypeRepository.findByCode(actorTypeCode)
                    .orElseThrow(() -> new RuntimeException("ActorType not found: " + actorTypeCode));

            // Create Log Entry
            ActivityLog logEntry = new ActivityLog();
            logEntry.setActor(actor);
            logEntry.setActorType(actorType);
            logEntry.setActionType(actionType);
            logEntry.setDescription(description);
            logEntry.setTargetTable(targetTable);
            logEntry.setTargetId(targetId);

            // Save Log Entry
            logRepository.save(logEntry);

        } catch (Exception e) {
            log.error("Unable to write activity log: {}", e.getMessage(), e);
        }
    }

    /**
     * === UTILITY FUNCTION (3 PARAMETERS) ===
     * Used for Authentication (SecurityEventListener)
     * Automatically assign targetTable="members" and targetId=actor.id
     *
     * @param email       Actor's email
     * @param actionCode  Action code (LOGIN, LOGOUT)
     * @param description Description of the action
     */
    @Transactional
    public void logActivity(String email, String actionCode, String description) {
        try {
            Member actor = memberRepository.findByEmailWithRoles(email).orElse(null);
            Long targetId = (actor != null) ? actor.getId() : null;

            logActivity(email, actionCode, description, Member.class.getSimpleName(), targetId);

        } catch (Exception e) {
            log.error("Unable to write activity log (auth): {}", e.getMessage(), e);
        }
    }
}
