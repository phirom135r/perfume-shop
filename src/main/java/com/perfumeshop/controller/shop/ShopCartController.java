package com.perfumeshop.controller.shop;

import com.perfumeshop.service.ShopCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("subtotal", cartService.getSubtotal(session));
        model.addAttribute("cartCount", cartService.getCartCount(session));
        return "shop/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestParam Long productId,
                                         @RequestParam(defaultValue = "1") int qty,
                                         HttpSession session) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            cartService.addToCart(productId, qty, session);
            res.put("ok", true);
            res.put("cartCount", cartService.getCartCount(session));
            res.put("subtotal", "$" + cartService.getSubtotal(session).setScale(2));
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
                                         HttpSession session) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            cartService.updateQty(productId, qty, session);

            var item = cartService.getItems(session).stream()
                    .filter(i -> i.getProductId().equals(productId))
                    .findFirst()
                    .orElse(null);

            res.put("ok", true);
            res.put("cartCount", cartService.getCartCount(session));
            res.put("subtotal", "$" + cartService.getSubtotal(session).setScale(2));
            res.put("qty", item != null ? item.getQty() : 0);
            res.put("lineTotal", item != null ? item.getLineTotal().setScale(2).toString() : "0.00");
        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeItem(@RequestParam Long productId,
                                          HttpSession session) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            cartService.removeItem(productId, session);
            res.put("ok", true);
            res.put("cartCount", cartService.getCartCount(session));
            res.put("subtotal", "$" + cartService.getSubtotal(session).setScale(2));
        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("/clear")
    @ResponseBody
    public Map<String, Object> clearCart(HttpSession session) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            cartService.clear(session);
            res.put("ok", true);
            res.put("cartCount", 0);
            res.put("subtotal", "$0.00");
        } catch (Exception e) {
            res.put("ok", false);
            res.put("message", e.getMessage());
        }
        return res;
    }
}