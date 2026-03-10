package com.perfumeshop.service;

import com.perfumeshop.entity.Customer;
import com.perfumeshop.repository.CustomerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public CustomerUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Email is required");
        }

        String cleanEmail = email.trim().toLowerCase();

        Customer c = customerRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        boolean enabled = Boolean.TRUE.equals(c.getEnabled()) && Boolean.TRUE.equals(c.getActive());

        /*
         * IMPORTANT
         * Google accounts do not have local password.
         * Spring Security still requires a password value.
         * So we provide a dummy password when password is null.
         */

        String password = c.getPassword();

        if (password == null || password.isBlank()) {
            password = "{noop}google-oauth2-user";
        }

        return new User(
                c.getEmail(),
                password,
                enabled,
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }
}