package com.example.member_management_system;

import com.example.member_management_system.entity.*;
import com.example.member_management_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PositionRepository positionRepository;
    private final ActionTypeRepository actionTypeRepository;
    private final ActorTypeRepository actorTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${DEFAULT_ADMIN_EMAIL:admin@mms.com}")
    private String defaultAdminEmail;

    @Value("${DEFAULT_ADMIN_PASSWORD:admin123}")
    private String defaultAdminPassword;

    @Override
    public void run(String... args) throws Exception {
        log.info("Seeding initial data...");

        Role adminRole = createRoleIfNotFound("ROLE_ADMIN");
        Role userRole = createRoleIfNotFound("ROLE_USER");

        Position adminPosition = createPositionIfNotFound("Administrator");

        createActionTypeIfNotFound("LOGIN", "User logged in");
        createActionTypeIfNotFound("LOGOUT", "User logged out");
        createActionTypeIfNotFound("CREATE", "Create record");
        createActionTypeIfNotFound("UPDATE", "Update record");
        createActionTypeIfNotFound("DELETE", "Delete record");
        createActionTypeIfNotFound("IMPORT", "Import data");
        createActionTypeIfNotFound("EXPORT", "Export data");
        createActorTypeIfNotFound("ADMIN", "Admin user");
        createActorTypeIfNotFound("USER", "Regular user");
        createActorTypeIfNotFound("SYSTEM", "System process");

        if (!memberRepository.existsByEmailIgnoreCase(defaultAdminEmail)) {
            Member admin = Member.builder()
                    .email(defaultAdminEmail)
                    .password(passwordEncoder.encode(defaultAdminPassword))
                    .fullName("Admin")
                    .isActive(true)
                    .position(adminPosition)
                    .roles(Set.of(adminRole))
                    .build();

            memberRepository.save(admin);
        }

        log.info("Seeding data done.");
    }

    private Role createRoleIfNotFound(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(name)
                                .description("Auto created role")
                                .build()
                ));
    }

    private Position createPositionIfNotFound(String name) {
        return positionRepository.findByName(name)
                .orElseGet(() -> {
                    Position pos = new Position();
                    pos.setName(name);
                    return positionRepository.save(pos);
                });
    }

    private void createActionTypeIfNotFound(String code, String desc) {
        if (actionTypeRepository.findByCode(code).isEmpty()) {
            ActionType type = new ActionType();
            type.setCode(code);
            type.setDescription(desc);
            actionTypeRepository.save(type);
        }
    }

    private void createActorTypeIfNotFound(String code, String desc) {
        if (actorTypeRepository.findByCode(code).isEmpty()) {
            ActorType type = new ActorType();
            type.setCode(code);
            type.setDescription(desc);
            actorTypeRepository.save(type);
        }
    }
}

