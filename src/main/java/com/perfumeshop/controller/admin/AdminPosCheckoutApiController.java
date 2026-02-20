// src/main/java/com/perfumeshop/controller/admin/AdminPosCheckoutApiController.java
package com.perfumeshop.controller.admin;

import com.perfumeshop.dto.CheckoutRequest;
import com.perfumeshop.dto.CheckoutResponse;
import com.perfumeshop.entity.Order;
import com.perfumeshop.enums.PaymentMethod;
import com.perfumeshop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/pos")
public class AdminPosCheckoutApiController {

    private final OrderService orderService;

    public AdminPosCheckoutApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest req){
        Order order = orderService.createPosOrder(req);

        if(order.getPayment().getMethod() == PaymentMethod.KHQR){
            // TODO: redirect to KHQR page when you implement QR generation
            return ResponseEntity.ok(new CheckoutResponse(order.getOrderNo(), "/admin/orders/"+order.getId()));
        }
        return ResponseEntity.ok(new CheckoutResponse(order.getOrderNo(), null));
    }
}