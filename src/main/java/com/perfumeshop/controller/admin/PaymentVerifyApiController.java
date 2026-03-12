package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Order;
import com.perfumeshop.entity.OrderItem;
import com.perfumeshop.entity.Product;
import com.perfumeshop.enums.OrderStatus;
import com.perfumeshop.service.OrderService;
import com.perfumeshop.service.PaymentVerifyService;
import com.perfumeshop.service.ProductService;
import com.perfumeshop.service.TelegramMessageBuilder;
import com.perfumeshop.service.TelegramService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/api/payments")
public class PaymentVerifyApiController {

    private final PaymentVerifyService paymentVerifyService;
    private final OrderService orderService;
    private final ProductService productService;
    private final TelegramService telegramService;
    private final TelegramMessageBuilder telegramMessageBuilder;

    public PaymentVerifyApiController(PaymentVerifyService paymentVerifyService,
                                      OrderService orderService,
                                      ProductService productService,
                                      TelegramService telegramService,
                                      TelegramMessageBuilder telegramMessageBuilder) {
        this.paymentVerifyService = paymentVerifyService;
        this.orderService = orderService;
        this.productService = productService;
        this.telegramService = telegramService;
        this.telegramMessageBuilder = telegramMessageBuilder;
    }

    @GetMapping("/verify")
    @Transactional
    public ResponseEntity<?> verify(@RequestParam String md5) {

        System.out.println("=== ADMIN VERIFY START ===");
        System.out.println("MD5 = " + md5);

        Order order = orderService.findByMd5OrNull(md5);
        if (order == null) {
            System.out.println("Order not found by md5");
            return ResponseEntity.ok(Map.of("status", "NOT_FOUND"));
        }

        System.out.println("Order found: " + order.getInvoice());
        System.out.println("Current status: " + order.getStatus());

        if (order.getStatus() == OrderStatus.PAID) {
            System.out.println("Order already PAID");
            return ResponseEntity.ok(Map.of("status", "PAID"));
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            System.out.println("Order already CANCELLED");
            return ResponseEntity.ok(Map.of("status", "CANCELLED"));
        }

        boolean paid = paymentVerifyService.isPaid(md5);
        System.out.println("Bakong paid result = " + paid);

        if (!paid) {
            System.out.println("Still pending");
            return ResponseEntity.ok(Map.of("status", "PENDING"));
        }

        Order full = orderService.findWithItemsOrThrow(order.getId());

        for (OrderItem it : full.getItems()) {
            Product p = it.getProduct();
            int qty = it.getQty();
            int stock = p.getStock() == null ? 0 : p.getStock();

            System.out.println("Checking stock: " + p.getName() + " | stock=" + stock + " | qty=" + qty);

            if (stock < qty) {
                System.out.println("OUT_OF_STOCK");
                return ResponseEntity.ok(Map.of("status", "OUT_OF_STOCK"));
            }
        }

        for (OrderItem it : full.getItems()) {
            Product p = it.getProduct();
            int qty = it.getQty();
            int stock = p.getStock() == null ? 0 : p.getStock();

            p.setStock(stock - qty);
            productService.save(p);

            System.out.println("Deducted stock: " + p.getName() + " -> " + p.getStock());
        }

        full.setStatus(OrderStatus.PAID);
        Order paidOrder = orderService.save(full);

        System.out.println("Order updated to PAID: " + paidOrder.getInvoice());

        telegramService.sendMessage(
                telegramMessageBuilder.buildPaidOrderMessage(paidOrder)
        );

        System.out.println("=== ADMIN VERIFY DONE ===");

        return ResponseEntity.ok(Map.of("status", "PAID"));
    }

    @PostMapping("/cancel")
    @Transactional
    public ResponseEntity<?> cancel(@RequestParam String md5) {

        System.out.println("=== ADMIN CANCEL START ===");
        System.out.println("MD5 = " + md5);

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

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelled = orderService.save(order);

        telegramService.sendMessage(
                telegramMessageBuilder.buildCancelledOrderMessage(cancelled)
        );

        System.out.println("Order cancelled: " + cancelled.getInvoice());
        System.out.println("=== ADMIN CANCEL DONE ===");

        return ResponseEntity.ok(Map.of("status", "CANCELLED"));
    }
}