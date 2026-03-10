package com.perfumeshop.service;

import com.perfumeshop.entity.Customer;
import com.perfumeshop.repository.CustomerRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final MailService mailService;

    public CustomerService(CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           JavaMailSender mailSender,
                           MailService mailService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.mailService = mailService;
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

    public void sendForgotPasswordLink(String email, String appBaseUrl) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        String cleanEmail = email.trim().toLowerCase();

        Customer customer = customerRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        customer.setResetToken(token);
        customer.setResetTokenExpiredAt(LocalDateTime.now().plusMinutes(30));
        customerRepository.save(customer);

        String resetLink = appBaseUrl + "/perfume-shop/auth/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(customer.getEmail());
        message.setSubject("Reset Your Password - Perfume Shop");
        message.setText(
                "Hello " + (customer.getFullName() != null ? customer.getFullName() : "Customer") + ",\n\n" +
                        "Click the link below to reset your password:\n" +
                        resetLink + "\n\n" +
                        "This link will expire in 30 minutes.\n\n" +
                        "If you did not request this, please ignore this email."
        );

        mailService.sendAsync(message);
    }

    public Customer findByResetTokenOrThrow(String token) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Invalid reset token");
        }

        Customer customer = customerRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (customer.getResetTokenExpiredAt() == null ||
                customer.getResetTokenExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset link has expired");
        }

        return customer;
    }

    public void resetPassword(String token, String newPassword, String confirmPassword) {
        Customer customer = findByResetTokenOrThrow(token);

        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("New password is required");
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new RuntimeException("Confirm password is required");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Confirm password does not match");
        }

        customer.setPassword(passwordEncoder.encode(newPassword));
        customer.setResetToken(null);
        customer.setResetTokenExpiredAt(null);

        customerRepository.save(customer);
    }
    public Customer loginWithGoogle(String email, String fullName, String providerId) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Google email is missing");
        }

        String cleanEmail = email.trim().toLowerCase();

        Customer customer = customerRepository.findByEmail(cleanEmail).orElse(null);

        if (customer == null) {
            Customer c = new Customer();
            c.setFullName(fullName != null && !fullName.isBlank() ? fullName.trim() : "Google User");
            c.setEmail(cleanEmail);
            c.setProvider("GOOGLE");
            c.setProviderId(providerId);
            c.setEnabled(true);
            c.setActive(true);
            c.setEmailVerified(true);
            c.setPassword(null); // Google account does not need local password
            return customerRepository.save(c);
        }

        if (customer.getProvider() == null || customer.getProvider().isBlank()) {
            customer.setProvider("GOOGLE");
        }

        if (customer.getProviderId() == null || customer.getProviderId().isBlank()) {
            customer.setProviderId(providerId);
        }

        if (customer.getFullName() == null || customer.getFullName().isBlank()) {
            customer.setFullName(fullName != null && !fullName.isBlank() ? fullName.trim() : "Google User");
        }

        customer.setEnabled(true);
        customer.setActive(true);
        customer.setEmailVerified(true);

        return customerRepository.save(customer);
    }
}