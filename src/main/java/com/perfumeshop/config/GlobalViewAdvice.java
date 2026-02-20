package com.perfumeshop.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalViewAdvice {

    @ModelAttribute("activeMenu")
    public String activeMenu(HttpServletRequest request) {
        String uri = request.getRequestURI(); // ok in Java layer

        if (uri.startsWith("/admin/dashboard")) return "dashboard";
        if (uri.startsWith("/admin/categories")) return "categories";
        if (uri.startsWith("/admin/products")) return "products";
        if (uri.startsWith("/admin/pos")) return "pos";
        if (uri.startsWith("/admin/orders")) return "orders";

        return "";
    }
}