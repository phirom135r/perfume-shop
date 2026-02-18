package com.perfumeshop.config;

import com.perfumeshop.entity.User;
import com.perfumeshop.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.boot.CommandLineRunner seedAdmin(UserRepository repo,
                                                                BCryptPasswordEncoder enc) {
        return args -> {
            if (!repo.existsByUsername("admin")) {

                User u = new User();
                u.setFullName("Touch Samnang");
                u.setUsername("admin");
                u.setPasswordHash(enc.encode("123456"));
                u.setRole("ADMIN");
                u.setActive(true);

                repo.save(u);

                System.out.println("✅ Admin user created: admin / 123456");
            }
        };
    }
}
