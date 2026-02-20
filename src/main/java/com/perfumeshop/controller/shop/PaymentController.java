package com.perfumeshop.controller.shop;

import com.perfumeshop.entity.Payment;
import com.perfumeshop.enums.PaymentStatus;
import com.perfumeshop.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository paymentRepo;

    public PaymentController(PaymentRepository paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    @GetMapping("/khqr/{paymentId}")
    public String khqrPage(@PathVariable Long paymentId, Model model) {
        Payment p = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        model.addAttribute("paymentId", p.getId());
        model.addAttribute("orderNo", p.getOrder().getOrderNo());
        model.addAttribute("amount", p.getAmount());
        model.addAttribute("khqrString", p.getKhqrString()); // must not be null for KHQR
        model.addAttribute("md5", p.getMd5());
        model.addAttribute("expireSeconds", 120); // you can store in DB if needed

        return "shop/khqr-checkout";
    }

    @GetMapping("/check/{paymentId}")
    @ResponseBody
    public ResponseEntity<?> check(@PathVariable Long paymentId) {
        Payment p = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Here you should call Bakong verify API by md5 and update DB.
        // For now we just return current DB status.
        return ResponseEntity.ok(java.util.Map.of(
                "status", p.getStatus().name(),
                "orderNo", p.getOrder().getOrderNo()
        ));
    }

    // helper you will call after verify success
    @PostMapping("/mark-paid/{paymentId}")
    @ResponseBody
    public ResponseEntity<?> markPaid(@PathVariable Long paymentId) {
        Payment p = paymentRepo.findById(paymentId).orElseThrow();
        p.setStatus(PaymentStatus.PAID);
        paymentRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("ok", true));
    }
}