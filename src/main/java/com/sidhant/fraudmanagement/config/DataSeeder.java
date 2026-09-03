package com.sidhant.fraudmanagement.config;

import com.sidhant.fraudmanagement.entity.User;
import com.sidhant.fraudmanagement.enums.UserRole;
import com.sidhant.fraudmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            userRepository.findById(1L).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode("password123"));
            userRepository.save(user);
             });

            userRepository.findById(2L).ifPresent(user -> {
                user.setPasswordHash(passwordEncoder.encode("password456"));
                userRepository.save(user);
                });

            userRepository.findById(3L).ifPresent(user -> {
                user.setPasswordHash(passwordEncoder.encode("admin123"));
                userRepository.save(user);
            });
            if (userRepository.count() == 0) {

                User user1 = new User(
                        "user1",
                        "user1@test.com",
                        passwordEncoder.encode("password123"),
                        passwordEncoder.encode("1234"),
                        UserRole.USER
                );

                User user2 = new User(
                        "user2",
                        "user2@test.com",
                        passwordEncoder.encode("password456"),
                        passwordEncoder.encode("5678"),
                        UserRole.USER
                );

                User admin = new User(
                        "FRM Admin",
                        "admin@frm.com",
                        passwordEncoder.encode("admin123"),
                        passwordEncoder.encode("9999"),
                        UserRole.ADMIN
                );

                userRepository.save(user1);
                userRepository.save(user2);
                userRepository.save(admin);
            }
        };
    }
}