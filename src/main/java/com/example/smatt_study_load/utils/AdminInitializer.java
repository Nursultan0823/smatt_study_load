package com.example.smatt_study_load.utils;


import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.smatt_study_load.enums.Role;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.UserRepository;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@example.com";

            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User();
                admin.setFullName("System Administrator");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setStatus(UserStatus.APPROVED);
                admin.setEnabled(true);
                admin.setGroupName(null);

                userRepository.save(admin);

                System.out.println("Администратор создан");
            } else {
                System.out.println("Администратор уже существует");
            }
        };
    }
}
