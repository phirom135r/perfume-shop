package com.perfumeshop.service;

import com.perfumeshop.entity.Customer;
import com.perfumeshop.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Customer register(String fullName,
                             String email,
                             String phone,
                             String password) {

        String safeEmail = email == null ? "" : email.trim().toLowerCase();

        if (fullName == null || fullName.isBlank()) {
            throw new RuntimeException("Full name is required");
        }

        if (safeEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (password == null || password.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        if (customerRepository.existsByEmail(safeEmail)) {
            throw new RuntimeException("Email already exists");
        }

        Customer c = new Customer();
        c.setFullName(fullName.trim());
        c.setEmail(safeEmail);
        c.setPhone(phone == null ? null : phone.trim());
        c.setPassword(passwordEncoder.encode(password));
        c.setEnabled(true);
        c.setEmailVerified(false);
        c.setProvider("LOCAL");

        return customerRepository.save(c);
    }

    public Customer findByEmailOrThrow(String email) {
        return customerRepository.findByEmail(email == null ? "" : email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }
}