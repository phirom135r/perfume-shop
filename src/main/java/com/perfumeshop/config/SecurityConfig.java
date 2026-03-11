package com.perfumeshop.config;

import com.perfumeshop.service.CustomerUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomerUserDetailsService customerUserDetailsService;
    private final GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(CustomerUserDetailsService customerUserDetailsService,
                          GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler,
                          PasswordEncoder passwordEncoder) {
        this.customerUserDetailsService = customerUserDetailsService;
        this.googleOAuth2SuccessHandler = googleOAuth2SuccessHandler;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public DaoAuthenticationProvider customerAuthProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customerUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/admin/**")
                )

                .authenticationProvider(customerAuthProvider())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").permitAll()

                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/auth/**",
                                "/perfume-shop",
                                "/perfume-shop/",
                                "/perfume-shop/shop",
                                "/perfume-shop/about",
                                "/perfume-shop/contact",
                                "/perfume-shop/product/**",
                                "/perfume-shop/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/perfume-shop/cart/add").permitAll()

                        .requestMatchers(
                                "/perfume-shop/cart",
                                "/perfume-shop/cart/update",
                                "/perfume-shop/cart/remove",
                                "/perfume-shop/cart/clear",
                                "/perfume-shop/checkout/**",
                                "/perfume-shop/my-orders/**",
                                "/perfume-shop/account/**",
                                "/perfume-shop/api/payment/**"
                        ).authenticated()

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

                .oauth2Login(oauth -> oauth
                        .loginPage("/perfume-shop/auth/login")
                        .successHandler(googleOAuth2SuccessHandler)
                )

                .logout(logout -> logout
                        .logoutUrl("/perfume-shop/auth/logout")
                        .logoutSuccessUrl("/perfume-shop/auth/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}