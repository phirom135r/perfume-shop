// src/main/java/com/perfumeshop/controller/admin/AdminPaymentVerifyApiController.java
package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Order;
import com.perfumeshop.entity.OrderItem;
import com.perfumeshop.entity.Product;
import com.perfumeshop.enums.OrderStatus;
import com.perfumeshop.service.OrderService;
import com.perfumeshop.service.PaymentVerifyService;
import com.perfumeshop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/api/payments")
public class AdminPaymentVerifyApiController {

    private final PaymentVerifyService paymentVerifyService;
    private final OrderService orderService;
    private final ProductService productService;

    public AdminPaymentVerifyApiController(PaymentVerifyService paymentVerifyService,
                                           OrderService orderService,
                                           ProductService productService) {
        this.paymentVerifyService = paymentVerifyService;
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping("/verify")
    @Transactional
    public ResponseEntity<?> verify(@RequestParam String md5) {

        Order order = orderService.findByMd5OrNull(md5);
        if (order == null) {
            return ResponseEntity.ok(Map.of("status", "NOT_FOUND"));
        }

        if (order.getStatus() == OrderStatus.PAID) {
            return ResponseEntity.ok(Map.of("status", "PAID"));
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return ResponseEntity.ok(Map.of("status", "CANCELLED"));
        }

        boolean paid = paymentVerifyService.isPaid(md5);

        if (paid) {
            // ✅ load full order (items + product)
            Order full = orderService.findWithItemsOrThrow(order.getId());

            // ✅ deduct stock now (only on successful payment)
            for (OrderItem it : full.getItems()) {
                Product p = it.getProduct();
                int qty = it.getQty();

                if (p.getStock() != null) {
                    if (p.getStock() < qty) {
                        return ResponseEntity.ok(Map.of("status", "OUT_OF_STOCK"));
                    }
                    p.setStock(p.getStock() - qty);
                    productService.save(p);
                }
            }

            full.setStatus(OrderStatus.PAID);
            orderService.save(full);

            return ResponseEntity.ok(Map.of("status", "PAID"));
        }

        return ResponseEntity.ok(Map.of("status", "PENDING"));
    }

    @PostMapping("/cancel")
    @Transactional
    public ResponseEntity<?> cancel(@RequestParam String md5) {

        Order order = orderService.findByMd5OrNull(md5);
        if (order == null) {
            return ResponseEntity.ok(Map.of("status", "NOT_FOUND"));
        }

        // if already paid, don't cancel
        if (order.getStatus() == OrderStatus.PAID) {
            return ResponseEntity.ok(Map.of("status", "PAID"));
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderService.save(order);

        return ResponseEntity.ok(Map.of("status", "CANCELLED"));
    }
}