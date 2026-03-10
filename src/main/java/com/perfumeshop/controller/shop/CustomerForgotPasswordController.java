package com.perfumeshop.controller.shop;

import com.perfumeshop.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/perfume-shop/auth")
public class CustomerForgotPasswordController {

    private final CustomerService customerService;

    public CustomerForgotPasswordController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "shop/auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    @ResponseBody
    public ResponseEntity<?> sendResetLink(@RequestParam String email,
                                           HttpServletRequest request) {
        Map<String, Object> res = new LinkedHashMap<>();

        try {
            String appBaseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            customerService.sendForgotPasswordLink(email, appBaseUrl);

            res.put("ok", true);
            res.put("message", "Reset link has been sent to your email.");
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        try {
            customerService.findByResetTokenOrThrow(token);
            model.addAttribute("token", token);
            return "shop/auth/reset-password";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "shop/auth/forgot-password";
        }
    }

    @PostMapping("/reset-password")
    public String doResetPassword(@RequestParam String token,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  Model model) {
        try {
            customerService.resetPassword(token, newPassword, confirmPassword);
            return "redirect:/perfume-shop/auth/login?resetSuccess";
        } catch (Exception e) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", e.getMessage());
            return "shop/auth/reset-password";
        }
    }
}