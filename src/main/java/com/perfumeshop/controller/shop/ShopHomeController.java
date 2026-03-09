package com.perfumeshop.controller.shop;

import com.perfumeshop.service.ProductService;
import com.perfumeshop.service.ShopCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShopHomeController {

    private final ProductService productService;
    private final ShopCartService cartService;

    public ShopHomeController(ProductService productService, ShopCartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping("/perfume-shop")
    public String home(Model model, HttpSession session) {
        var products = productService.search(
                "",
                null,
                true,
                PageRequest.of(0, 8, Sort.by("id").descending())
        ).getContent();

        model.addAttribute("products", products);
        model.addAttribute("cartCount", cartService.getCartCount(session));
        return "shop/home";
    }
}