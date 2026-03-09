package com.perfumeshop.controller.shop;

import com.perfumeshop.dto.ShopCheckoutForm;
import com.perfumeshop.entity.Order;
import com.perfumeshop.service.ShopCartService;
import com.perfumeshop.service.ShopOrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/perfume-shop/checkout")
public class ShopCheckoutController {

    private final ShopCartService cartService;
    private final ShopOrderService shopOrderService;

    public ShopCheckoutController(ShopCartService cartService,
                                  ShopOrderService shopOrderService) {
        this.cartService = cartService;
        this.shopOrderService = shopOrderService;
    }

    @GetMapping
    public String checkoutPage(Model model, HttpSession session) {
        var items = cartService.getItems(session);
        BigDecimal subtotal = cartService.getSubtotal(session);
        int cartCount = cartService.getCartCount(session);

        if (items == null || items.isEmpty()) {
            return "redirect:/perfume-shop/cart";
        }

        if (!model.containsAttribute("form")) {
            ShopCheckoutForm form = new ShopCheckoutForm();
            model.addAttribute("form", form);
        }

        model.addAttribute("items", items);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("cartCount", cartCount);

        return "shop/checkout";
    }

    @PostMapping
    public String placeOrder(@ModelAttribute("form") ShopCheckoutForm form,
                             HttpSession session,
                             Model model,
                             Authentication authentication) {
        try {
            String customerEmail = authentication.getName();
            Order saved = shopOrderService.placeOrder(form, session, customerEmail);
            return "redirect:/perfume-shop/checkout/success?orderId=" + saved.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("items", cartService.getItems(session));
            model.addAttribute("subtotal", cartService.getSubtotal(session));
            model.addAttribute("cartCount", cartService.getCartCount(session));
            return "shop/checkout";
        }
    }

    @GetMapping("/success")
    public String successPage(@RequestParam Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "shop/order-success";
    }
}