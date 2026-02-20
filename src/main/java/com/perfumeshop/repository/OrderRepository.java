package com.perfumeshop.repository;

import com.perfumeshop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByOrderNo(String orderNo);
    Optional<Order> findByOrderNo(String orderNo);
}