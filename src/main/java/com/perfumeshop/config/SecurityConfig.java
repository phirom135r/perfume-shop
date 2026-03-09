package com.perfumeshop.config;

import com.perfumeshop.service.CustomerUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomerUserDetailsService customerUserDetailsService;

    public SecurityConfig(CustomerUserDetailsService customerUserDetailsService) {
        this.customerUserDetailsService = customerUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider customerAuthProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customerUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authenticationProvider(customerAuthProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/auth/**",
                                "/perfume-shop",
                                "/perfume-shop/auth/**",
                                "/perfume-shop/product/**"
                        ).permitAll()

                        // ✅ allow guest to call add-to-cart endpoint,
                        // controller will return loginRequired=true
                        .requestMatchers(HttpMethod.POST, "/perfume-shop/cart/add").permitAll()

                        // ✅ real protected customer pages
                        .requestMatchers(
                                "/perfume-shop/cart",
                                "/perfume-shop/checkout/**",
                                "/perfume-shop/my-orders/**",
                                "/perfume-shop/account/**"
                        ).hasRole("CUSTOMER")

                        // update/remove/clear should be customer only
                        .requestMatchers(
                                "/perfume-shop/cart/update",
                                "/perfume-shop/cart/remove",
                                "/perfume-shop/cart/clear"
                        ).hasRole("CUSTOMER")

                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/perfume-shop/auth/login")
                        .loginProcessingUrl("/perfume-shop/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/perfume-shop", true)
                        .failureUrl("/perfume-shop/auth/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/perfume-shop/auth/logout")
                        .logoutSuccessUrl("/perfume-shop/auth/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}