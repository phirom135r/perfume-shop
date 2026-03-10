package com.perfumeshop.controller.shop;

import com.perfumeshop.config.SecurityHelper;
import com.perfumeshop.entity.Customer;
import com.perfumeshop.service.CustomerService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perfume-shop/account")
public class ShopAccountController {

    private final CustomerService customerService;

    public ShopAccountController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String accountPage(Authentication authentication, Model model) {
        String email = SecurityHelper.currentCustomerEmail(authentication);
        Customer customer = customerService.findByEmailOrThrow(email);

        model.addAttribute("customer", customer);
        return "shop/account";
    }

    @PostMapping
    public String updateAccount(@RequestParam String fullName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                Authentication authentication,
                                Model model) {
        try {
            String email = SecurityHelper.currentCustomerEmail(authentication);
            Customer updated = customerService.updateProfile(email, fullName, phone, address);

            model.addAttribute("customer", updated);
            model.addAttribute("successMessage", "Profile updated successfully.");
            return "shop/account";

        } catch (Exception e) {
            String email = SecurityHelper.currentCustomerEmail(authentication);
            Customer customer = customerService.findByEmailOrThrow(email);

            model.addAttribute("customer", customer);
            model.addAttribute("errorMessage", e.getMessage());
            return "shop/account";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 Model model) {
        String email = SecurityHelper.currentCustomerEmail(authentication);

        try {
            customerService.changePassword(email, currentPassword, newPassword, confirmPassword);

            Customer customer = customerService.findByEmailOrThrow(email);
            model.addAttribute("customer", customer);
            model.addAttribute("passwordSuccessMessage", "Password changed successfully.");
            return "shop/account";

        } catch (Exception e) {
            Customer customer = customerService.findByEmailOrThrow(email);
            model.addAttribute("customer", customer);
            model.addAttribute("passwordErrorMessage", e.getMessage());
            return "shop/account";
        }
    }
}