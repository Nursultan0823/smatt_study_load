package com.example.smatt_study_load.utils;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.smatt_study_load.enums.Role;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.Roles;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.RoleRepository;
import com.example.smatt_study_load.repository.UserRepository;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder,
                                       RoleRepository roleRepository) {
        return args -> {
            String adminEmail = "admin@example.com";

            for (Role roleName : Role.values()) {
                if (!roleRepository.existsByName(roleName)) {
                    Roles role = new Roles();
                    role.setName(roleName);
                    roleRepository.save(role);
                }
            }

            if (!userRepository.existsByEmail(adminEmail)) {
                Roles adminRole = roleRepository.findByName(Role.ADMIN)
                        .orElseThrow(() -> new RuntimeException("Роль ADMIN не найдена"));

                Set<Roles> adminRoles = new HashSet<>();
                adminRoles.add(adminRole);

                User admin = new User();
                admin.setFullName("System Administrator");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRoles(adminRoles);
                admin.setStatus(UserStatus.APPROVED);
                admin.setEnabled(true);

                userRepository.save(admin);

                System.out.println("Администратор создан");
            } else {
                System.out.println("Администратор уже существует");
            }
        };
    }
}