package com.perfumeshop.config;

import com.perfumeshop.service.ShopCartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalViewAdvice {

    private final ShopCartService shopCartService;

    public GlobalViewAdvice(ShopCartService shopCartService) {
        this.shopCartService = shopCartService;
    }

    @ModelAttribute("activeMenu")
    public String activeMenu(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.startsWith("/admin/dashboard")) return "dashboard";
        if (uri.startsWith("/admin/brands")) return "brands";
        if (uri.startsWith("/admin/categories")) return "categories";
        if (uri.startsWith("/admin/products")) return "products";
        if (uri.startsWith("/admin/pos")) return "pos";
        if (uri.startsWith("/admin/orders")) return "orders";
        if (uri.startsWith("/admin/stock")) return "stock";

        return "";
    }

    @ModelAttribute
    public void globalShopData(Model model,
                               Authentication authentication,
                               HttpSession session) {

        boolean customerLoggedIn =
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("customerLoggedIn", customerLoggedIn);

        if (customerLoggedIn) {
            model.addAttribute("customerEmail", authentication.getName());
        }

        model.addAttribute("cartCount", shopCartService.getCartCount(session));
    }
}