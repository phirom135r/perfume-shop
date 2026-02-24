package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Order;
import com.perfumeshop.enums.OrderStatus;
import com.perfumeshop.service.OrderService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String page(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by("id").descending()
        );

        Page<Order> orders = orderService.adminSearch(q, status, pageable);

        model.addAttribute("orders", orders);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("activeMenu", "orders");
        return "admin/orders";
    }
}