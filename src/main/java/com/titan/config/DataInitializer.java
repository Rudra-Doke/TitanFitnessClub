package com.titan.config;

import com.titan.entity.Role;
import com.titan.entity.User;
import com.titan.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (!userRepository.existsByUsername("admin")) {

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);

            userRepository.save(admin);

            System.out.println("Admin created successfully.");
        }

        if (!userRepository.existsByUsername("member")) {

            User member = new User();
            member.setUsername("member");
            member.setPassword(passwordEncoder.encode("member123"));
            member.setRole(Role.MEMBER);
            member.setEnabled(true);

            userRepository.save(member);

            System.out.println("Member created successfully.");
        }
    }
}