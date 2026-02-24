package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Order;
import com.perfumeshop.enums.OrderStatus;
import com.perfumeshop.service.OrderService;
import com.perfumeshop.service.PaymentVerifyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/api/payments")
public class AdminPaymentVerifyApiController {

    private final PaymentVerifyService paymentVerifyService;
    private final OrderService orderService;

    public AdminPaymentVerifyApiController(PaymentVerifyService paymentVerifyService,
                                           OrderService orderService) {
        this.paymentVerifyService = paymentVerifyService;
        this.orderService = orderService;
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String md5) {

        Order order = orderService.findByMd5OrNull(md5);
        if (order == null) {
            return ResponseEntity.ok(Map.of("status", "NOT_FOUND"));
        }

        // ✅ already completed
        if (order.getStatus() == OrderStatus.PAID) {
            return ResponseEntity.ok(Map.of("status", "PAID"));
        }

        boolean paid = paymentVerifyService.isPaid(md5);

        if (paid) {
            order.setStatus(OrderStatus.PAID);
            orderService.save(order);
            return ResponseEntity.ok(Map.of("status", "PAID"));
        }

        return ResponseEntity.ok(Map.of("status", "PENDING"));
    }
}