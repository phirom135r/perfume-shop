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

    public Customer register(String fullName, String email, String phone, String password) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        String cleanEmail = email.trim().toLowerCase();

        if (customerRepository.existsByEmail(cleanEmail)) {
            throw new RuntimeException("Email already exists");
        }

        Customer c = new Customer();
        c.setFullName(fullName);
        c.setEmail(cleanEmail);
        c.setPhone(phone);
        c.setPassword(passwordEncoder.encode(password));
        c.setActive(true);

        return customerRepository.save(c);
    }

    public Customer findByEmailOrThrow(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + email));
    }

    public Customer updateProfile(String email, String fullName, String phone, String address) {
        Customer c = findByEmailOrThrow(email);

        if (fullName == null || fullName.isBlank()) {
            throw new RuntimeException("Full name is required");
        }

        c.setFullName(fullName.trim());
        c.setPhone(phone != null ? phone.trim() : null);
        c.setAddress(address != null ? address.trim() : null);

        return customerRepository.save(c);
    }
    public void changePassword(String email,
                               String currentPassword,
                               String newPassword,
                               String confirmPassword) {

        Customer c = findByEmailOrThrow(email);

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("Current password is required");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("New password is required");
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new RuntimeException("Confirm password is required");
        }

        if (!passwordEncoder.matches(currentPassword, c.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Confirm password does not match");
        }

        if (passwordEncoder.matches(newPassword, c.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        c.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(c);
    }
}