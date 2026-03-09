package com.perfumeshop.controller.shop;

import com.perfumeshop.service.CustomerService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfume-shop/auth")
public class CustomerAuthController {

    private final CustomerService customerService;

    public CustomerAuthController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/login")
    public String loginPage(Authentication authentication,
                            @RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {

        if (authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/perfume-shop";
        }

        model.addAttribute("error", error != null);
        model.addAttribute("logout", logout != null);
        return "shop/auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Authentication authentication) {
        if (authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/perfume-shop";
        }
        return "shop/auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam(required = false) String phone,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             RedirectAttributes ra) {

        try {
            if (!password.equals(confirmPassword)) {
                throw new RuntimeException("Confirm password does not match");
            }

            customerService.register(fullName, email, phone, password);

            ra.addFlashAttribute("successMessage", "Register successful. Please login.");
            return "redirect:/perfume-shop/auth/login";

        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            ra.addFlashAttribute("fullName", fullName);
            ra.addFlashAttribute("email", email);
            ra.addFlashAttribute("phone", phone);
            return "redirect:/perfume-shop/auth/register";
        }
    }
}