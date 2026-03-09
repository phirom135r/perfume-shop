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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Customer c = customerRepository.findByEmail(username.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        return new User(
                c.getEmail(),
                c.getPassword(),
                Boolean.TRUE.equals(c.getEnabled()),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }
}