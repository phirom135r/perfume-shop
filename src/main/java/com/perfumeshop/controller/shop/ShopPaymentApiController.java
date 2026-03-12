package com.perfumeshop.controller.shop;

import com.perfumeshop.entity.Order;
import com.perfumeshop.enums.OrderStatus;
import com.perfumeshop.service.OrderService;
import com.perfumeshop.service.PaymentVerifyService;
import com.perfumeshop.service.ProductService;
import com.perfumeshop.service.ShopCartService;
import com.perfumeshop.service.TelegramMessageBuilder;
import com.perfumeshop.service.TelegramService;
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
    private final TelegramService telegramService;
    private final TelegramMessageBuilder telegramMessageBuilder;

    public ShopPaymentApiController(OrderService orderService,
                                    PaymentVerifyService paymentVerifyService,
                                    ProductService productService,
                                    ShopCartService shopCartService,
                                    TelegramService telegramService,
                                    TelegramMessageBuilder telegramMessageBuilder) {
        this.orderService = orderService;
        this.paymentVerifyService = paymentVerifyService;
        this.productService = productService;
        this.shopCartService = shopCartService;
        this.telegramService = telegramService;
        this.telegramMessageBuilder = telegramMessageBuilder;
    }

    @GetMapping("/verify")
    @Transactional
    public ResponseEntity<?> verify(@RequestParam String md5, HttpSession session) {
        System.out.println("SHOP VERIFY md5 = " + md5);

        Order order = orderService.findByMd5OrNull(md5);

        if (order == null) {
            System.out.println("SHOP VERIFY RESULT = NOT_FOUND");
            return ResponseEntity.ok(Map.of("status", "NOT_FOUND"));
        }

        if (order.getStatus() == OrderStatus.PAID) {
            System.out.println("SHOP VERIFY RESULT = ALREADY PAID");
            return ResponseEntity.ok(Map.of(
                    "status", "PAID",
                    "orderId", order.getId()
            ));
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            System.out.println("SHOP VERIFY RESULT = CANCELLED");
            return ResponseEntity.ok(Map.of("status", "CANCELLED"));
        }

        boolean paid = paymentVerifyService.isPaid(md5);
        System.out.println("SHOP VERIFY BAKONG PAID = " + paid);

        if (!paid) {
            return ResponseEntity.ok(Map.of("status", "PENDING"));
        }

        Order full = orderService.findWithItemsOrThrow(order.getId());

        for (var it : full.getItems()) {
            var p = it.getProduct();
            int qty = it.getQty();

            int stock = p.getStock() == null ? 0 : p.getStock();
            if (stock < qty) {
                System.out.println("SHOP VERIFY RESULT = OUT_OF_STOCK");
                return ResponseEntity.ok(Map.of("status", "OUT_OF_STOCK"));
            }
        }

        for (var it : full.getItems()) {
            var p = it.getProduct();
            int qty = it.getQty();

            int stock = p.getStock() == null ? 0 : p.getStock();
            p.setStock(stock - qty);
            productService.save(p);
        }

        full.setStatus(OrderStatus.PAID);
        Order paidOrder = orderService.save(full);

        shopCartService.clear(session);

        telegramService.sendMessage(
                telegramMessageBuilder.buildPaidOrderMessage(paidOrder)
        );

        System.out.println("SHOP VERIFY RESULT = PAID, orderId = " + paidOrder.getId());

        return ResponseEntity.ok(Map.of(
                "status", "PAID",
                "orderId", paidOrder.getId()
        ));
    }

    @PostMapping("/cancel")
    @Transactional
    public ResponseEntity<?> cancel(@RequestParam String md5) {
        System.out.println("SHOP CANCEL md5 = " + md5);

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
        Order cancelledOrder = orderService.save(order);

        telegramService.sendMessage(
                telegramMessageBuilder.buildCancelledOrderMessage(cancelledOrder)
        );

        return ResponseEntity.ok(Map.of("status", "CANCELLED"));
    }
}