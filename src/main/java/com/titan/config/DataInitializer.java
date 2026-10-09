package com.titan.config;

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

        String username = System.getenv("TITAN_SEED_ADMIN_USERNAME");
        String password = System.getenv("TITAN_SEED_ADMIN_PASSWORD");
        if (username == null || username.isBlank() || password == null || password.isBlank()) return;
        if (userRepository.existsByUsername(username)) return;

        User admin = new User();
        admin.setUsername(username.trim());
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(com.titan.entity.Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);
    }
}
