// src/main/java/com/perfumeshop/repository/PaymentRepository.java
package com.perfumeshop.repository;

import com.perfumeshop.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {}