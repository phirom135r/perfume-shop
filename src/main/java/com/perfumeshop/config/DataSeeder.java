package com.perfumeshop.config;

import com.perfumeshop.entity.User;
import com.perfumeshop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedAdmin(UserRepository repo,
                                       PasswordEncoder passwordEncoder) {
        return args -> {

            if (!repo.existsByUsername("admin")) {
                User u = new User();
                u.setFullName("Touch Samnang");
                u.setUsername("admin");
                u.setPasswordHash(passwordEncoder.encode("123456"));
                u.setRole("ADMIN");
                u.setActive(true);
                repo.save(u);
            }

            if (!repo.existsByUsername("nuch")) {
                User u1 = new User();
                u1.setFullName("Nu Nuch");
                u1.setUsername("nuch");
                u1.setPasswordHash(passwordEncoder.encode("654321"));
                u1.setRole("ADMIN");
                u1.setActive(true);
                repo.save(u1);
            }
        };
    }
}