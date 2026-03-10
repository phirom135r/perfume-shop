package com.perfumeshop.repository;

import com.perfumeshop.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByResetToken(String resetToken);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByProviderId(String providerId);
    boolean existsByEmail(String email);
}