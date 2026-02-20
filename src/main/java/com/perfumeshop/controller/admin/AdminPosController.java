//package com.perfumeshop.controller.admin;
//
//import com.perfumeshop.dto.CheckoutResponse;
//import com.perfumeshop.dto.PosCheckoutRequest;
//import com.perfumeshop.service.OrderService;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//@RequestMapping("/admin/pos")
//public class AdminPosController {
//
//    private final OrderService orderService;
//
//    public AdminPosController(OrderService orderService) {
//        this.orderService = orderService;
//    }
//
//    @GetMapping
//    public String page() {
//        return "admin/pos"; // create later
//    }
//
//    @PostMapping("/checkout")
//    @ResponseBody
//    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody PosCheckoutRequest req) {
//        CheckoutResponse res = orderService.placePosOrder(req);
//        return ResponseEntity.ok(res);
//    }
//}