package com.perfumeshop.controller.shop;

import com.perfumeshop.service.ShopCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/perfume-shop/cart")
public class ShopCartController {

    private final ShopCartService cartService;

    public ShopCartController(ShopCartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String cartPage(Model model, HttpSession session) {
        model.addAttribute("items", cartService.getItems(session));
        model.addAttribute("cartCount", cartService.getCartCount(session));

        model.addAttribute("subtotal", cartService.getOriginalSubtotal(session));
        model.addAttribute("discount", cartService.getDiscount(session));
        model.addAttribute("total", cartService.getSubtotal(session));

        return "shop/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestParam Long productId,
                                         @RequestParam(defaultValue = "1") int qty,
                                         HttpSession session,
                                         Authentication authentication) {
        Map<String, Object> res = new LinkedHashMap<>();

        boolean customerLoggedIn =
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !(authentication instanceof AnonymousAuthenticationToken);

        if (!customerLoggedIn) {
            res.put("ok", false);
            res.put("loginRequired", true);
            res.put("message", "Please login first");
            return res;
        }

        try {
            cartService.addToCart(productId, qty, session);
            res.put("ok", true);
            res.put("cartCount", cartService.getCartCount(session));
            res.put("subtotal", money(cartService.getOriginalSubtotal(session)));
            res.put("discount", "-" + moneyNoSign(cartService.getDiscount(session)));
            res.put("total", money(cartService.getSubtotal(session)));
            res.put("message", "Added to cart");
        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateQty(@RequestParam Long productId,
                                         @RequestParam int qty,
                                         HttpSession session,
                                         Authentication authentication) {
        Map<String, Object> res = new LinkedHashMap<>();

        boolean customerLoggedIn =
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !(authentication instanceof AnonymousAuthenticationToken);

        if (!customerLoggedIn) {
            res.put("ok", false);
            res.put("loginRequired", true);
            res.put("message", "Please login first");
            return res;
        }

        try {
            cartService.updateQty(productId, qty, session);

            var item = cartService.getItems(session).stream()
                    .filter(i -> i.getProductId().equals(productId))
                    .findFirst()
                    .orElse(null);

            res.put("ok", true);
            res.put("cartCount", cartService.getCartCount(session));
            res.put("subtotal", money(cartService.getOriginalSubtotal(session)));
            res.put("discount", "-" + moneyNoSign(cartService.getDiscount(session)));
            res.put("total", money(cartService.getSubtotal(session)));

            res.put("qty", item != null ? item.getQty() : 0);
            res.put("lineTotal", item != null
                    ? item.getLineTotal().setScale(2, RoundingMode.HALF_UP).toString()
                    : "0.00");

            res.put("originalPrice", item != null
                    ? item.getOriginalPrice().setScale(2, RoundingMode.HALF_UP).toString()
                    : "0.00");

            res.put("unitPrice", item != null
                    ? item.getUnitPrice().setScale(2, RoundingMode.HALF_UP).toString()
                    : "0.00");

            res.put("discountAmount", item != null
                    ? item.getDiscountAmount().setScale(2, RoundingMode.HALF_UP).toString()
                    : "0.00");

        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeItem(@RequestParam Long productId,
                                          HttpSession session,
                                          Authentication authentication) {
        Map<String, Object> res = new LinkedHashMap<>();

        boolean customerLoggedIn =
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !(authentication instanceof AnonymousAuthenticationToken);

        if (!customerLoggedIn) {
            res.put("ok", false);
            res.put("loginRequired", true);
            res.put("message", "Please login first");
            return res;
        }

        try {
            cartService.removeItem(productId, session);
            res.put("ok", true);
            res.put("cartCount", cartService.getCartCount(session));
            res.put("subtotal", money(cartService.getOriginalSubtotal(session)));
            res.put("discount", "-" + moneyNoSign(cartService.getDiscount(session)));
            res.put("total", money(cartService.getSubtotal(session)));
        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("/clear")
    @ResponseBody
    public Map<String, Object> clearCart(HttpSession session,
                                         Authentication authentication) {
        Map<String, Object> res = new LinkedHashMap<>();

        boolean customerLoggedIn =
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !(authentication instanceof AnonymousAuthenticationToken);

        if (!customerLoggedIn) {
            res.put("ok", false);
            res.put("loginRequired", true);
            res.put("message", "Please login first");
            return res;
        }

        try {
            cartService.clear(session);
            res.put("ok", true);
            res.put("cartCount", 0);
            res.put("subtotal", "$0.00");
            res.put("discount", "-$0.00");
            res.put("total", "$0.00");
        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    private String money(java.math.BigDecimal value) {
        if (value == null) return "$0.00";
        return "$" + value.setScale(2, RoundingMode.HALF_UP);
    }

    private String moneyNoSign(java.math.BigDecimal value) {
        if (value == null) return "$0.00";
        return "$" + value.setScale(2, RoundingMode.HALF_UP);
    }
}