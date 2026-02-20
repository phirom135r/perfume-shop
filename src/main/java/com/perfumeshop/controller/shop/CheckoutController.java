package com.perfumeshop.controller.shop;

import com.perfumeshop.dto.CheckoutResponse;
import com.perfumeshop.dto.WebCheckoutRequest;
import com.perfumeshop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final OrderService orderService;

    public CheckoutController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody WebCheckoutRequest req) {
        CheckoutResponse res = orderService.placeWebOrder(req);
        return ResponseEntity.ok(res);
    }
}