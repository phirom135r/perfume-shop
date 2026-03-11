package com.perfumeshop.controller.shop;

import com.perfumeshop.config.SecurityHelper;
import com.perfumeshop.dto.ShopCheckoutForm;
import com.perfumeshop.entity.Customer;
import com.perfumeshop.entity.Order;
import com.perfumeshop.enums.PaymentMethod;
import com.perfumeshop.service.CustomerService;
import com.perfumeshop.service.ShopCartService;
import com.perfumeshop.service.ShopOrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/perfume-shop/checkout")
public class ShopCheckoutController {

    private final ShopCartService cartService;
    private final ShopOrderService shopOrderService;
    private final CustomerService customerService;

    public ShopCheckoutController(ShopCartService cartService,
                                  ShopOrderService shopOrderService,
                                  CustomerService customerService) {
        this.cartService = cartService;
        this.shopOrderService = shopOrderService;
        this.customerService = customerService;
    }

    @GetMapping
    public String checkoutPage(Model model,
                               HttpSession session,
                               Authentication authentication) {

        var items = cartService.getItems(session);
        BigDecimal subtotal = cartService.getOriginalSubtotal(session);
        BigDecimal discount = cartService.getDiscount(session);
        BigDecimal total = cartService.getSubtotal(session);
        int cartCount = cartService.getCartCount(session);

        if (items == null || items.isEmpty()) {
            return "redirect:/perfume-shop/cart";
        }

        String customerEmail = SecurityHelper.currentCustomerEmail(authentication);
        model.addAttribute("customerEmail", customerEmail);

        if (!model.containsAttribute("form")) {
            Customer customer = customerService.findByEmailOrThrow(customerEmail);

            ShopCheckoutForm form = new ShopCheckoutForm();
            form.setFullName(customer.getFullName());
            form.setPhone(customer.getPhone());
            form.setAddress(customer.getAddress());
            form.setPaymentMethod("CASH");

            model.addAttribute("form", form);
        }

        model.addAttribute("items", items);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("total", total);
        model.addAttribute("cartCount", cartCount);

        return "shop/checkout";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> placeOrder(@ModelAttribute ShopCheckoutForm form,
                                        HttpSession session,
                                        Authentication authentication) {
        Map<String, Object> res = new LinkedHashMap<>();

        try {
            String customerEmail = SecurityHelper.currentCustomerEmail(authentication);
            Order saved = shopOrderService.placeOrder(form, session, customerEmail);

            res.put("ok", true);
            res.put("orderId", saved.getId());
            res.put("paymentMethod", saved.getPaymentMethod() != null ? saved.getPaymentMethod().name() : "CASH");

            if (saved.getPaymentMethod() == PaymentMethod.KHQR) {
                res.put("md5", saved.getMd5());
                res.put("khqrString", saved.getKhqrString());
                res.put("total", saved.getTotal());
                res.put("invoice", saved.getInvoice());
                res.put("status", saved.getStatus() != null ? saved.getStatus().name() : "PENDING");
            }

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
}