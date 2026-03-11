package com.perfumeshop.controller.shop;

import com.perfumeshop.entity.Order;
import com.perfumeshop.enums.OrderStatus;
import com.perfumeshop.service.OrderService;
import com.perfumeshop.service.PaymentVerifyService;
import com.perfumeshop.service.ShopCartService;
import com.perfumeshop.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/perfume-shop/api/payment")
public class ShopPaymentApiController {

    private final OrderService orderService;
    private final PaymentVerifyService paymentVerifyService;
    private final ProductService productService;
    private final ShopCartService shopCartService;

    public ShopPaymentApiController(OrderService orderService,
                                    PaymentVerifyService paymentVerifyService,
                                    ProductService productService,
                                    ShopCartService shopCartService) {
        this.orderService = orderService;
        this.paymentVerifyService = paymentVerifyService;
        this.productService = productService;
        this.shopCartService = shopCartService;
    }

    @GetMapping("/verify")
    @Transactional
    public ResponseEntity<?> verify(@RequestParam String md5, HttpSession session) {
        Order order = orderService.findByMd5OrNull(md5);

        if (order == null) {
            return ResponseEntity.ok(Map.of("status", "NOT_FOUND"));
        }

        if (order.getStatus() == OrderStatus.PAID) {
            return ResponseEntity.ok(Map.of(
                    "status", "PAID",
                    "orderId", order.getId()
            ));
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return ResponseEntity.ok(Map.of("status", "CANCELLED"));
        }

        boolean paid = paymentVerifyService.isPaid(md5);

        if (paid) {
            Order full = orderService.findWithItemsOrThrow(order.getId());

            for (var it : full.getItems()) {
                var p = it.getProduct();
                int qty = it.getQty();

                int stock = p.getStock() == null ? 0 : p.getStock();
                if (stock < qty) {
                    return ResponseEntity.ok(Map.of("status", "OUT_OF_STOCK"));
                }

                p.setStock(stock - qty);
                productService.save(p);
            }

            full.setStatus(OrderStatus.PAID);
            orderService.save(full);

            shopCartService.clear(session);

            return ResponseEntity.ok(Map.of(
                    "status", "PAID",
                    "orderId", full.getId()
            ));
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

        if (order.getStatus() == OrderStatus.PAID) {
            return ResponseEntity.ok(Map.of(
                    "status", "PAID",
                    "orderId", order.getId()
            ));
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderService.save(order);

        return ResponseEntity.ok(Map.of("status", "CANCELLED"));
    }
}