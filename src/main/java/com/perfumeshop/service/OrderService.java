package com.perfumeshop.service;

import com.perfumeshop.dto.OrderRowDto;
import com.perfumeshop.entity.Order;
import com.perfumeshop.enums.OrderStatus;
import com.perfumeshop.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository repo;

    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }

    public Order save(Order o) {
        return repo.save(o);
    }

    public Order findByIdOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public Order findByMd5OrNull(String md5) {
        return repo.findByMd5(md5).orElse(null);
    }

    public Page<Order> adminSearch(String q, OrderStatus status, Pageable pageable) {
        return repo.adminSearch(q, status, pageable);
    }

    public Order findWithItemsOrThrow(Long id) {
        return repo.findWithItemsById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public Order markPaid(Order order) {
        if (order == null) return null;
        order.setStatus(OrderStatus.PAID);
        return repo.save(order);
    }

    public Page<OrderRowDto> adminSearchRows(String q, OrderStatus status, Pageable pageable) {
        return repo.adminSearchRows(q, status, pageable);
    }

    public java.util.List<Order> findMyOrders(String email) {
        return repo.findByCustomerEmailOrderByCreatedAtDesc(email);
    }

    public Order findMyOrderDetailOrThrow(Long id, String email) {
        return repo.findDetailByIdAndCustomerEmail(id, email)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order findInvoiceForAdminOrThrow(Long id) {
        return findWithItemsOrThrow(id);
    }

    public Order findInvoiceForCustomerOrThrow(Long id, String email) {
        return findMyOrderDetailOrThrow(id, email);
    }
}