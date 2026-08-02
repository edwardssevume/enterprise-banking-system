package com.enterprisebank.auth.config;

import com.enterprisebank.auth.entity.Role;
import com.enterprisebank.auth.entity.RoleName;
import com.enterprisebank.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRoleIfNotExists(RoleName.ROLE_CUSTOMER);
        createRoleIfNotExists(RoleName.ROLE_EMPLOYEE);
        createRoleIfNotExists(RoleName.ROLE_ADMIN);
    }

    private void createRoleIfNotExists(RoleName roleName) {

        if (!roleRepository.findByName(roleName).isPresent()) {

            Role role = Role.builder()
                    .name(roleName)
                    .build();

            roleRepository.save(role);
        }
    }
}
