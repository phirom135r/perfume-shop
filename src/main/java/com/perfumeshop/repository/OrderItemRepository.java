// src/main/java/com/perfumeshop/repository/OrderItemRepository.java
package com.perfumeshop.repository;

import com.perfumeshop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}