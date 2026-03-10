package com.perfumeshop.controller.shop;

import com.perfumeshop.config.SecurityHelper;
import com.perfumeshop.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perfume-shop/my-orders")
public class ShopMyOrderController {

    private final OrderService orderService;

    public ShopMyOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String myOrders(Authentication authentication, Model model) {
        String email = SecurityHelper.currentCustomerEmail(authentication);
        model.addAttribute("orders", orderService.findMyOrders(email));
        return "shop/my-orders";
    }

    @GetMapping("/{id}")
    public String myOrderDetail(@PathVariable Long id,
                                Authentication authentication,
                                Model model) {
        String email = SecurityHelper.currentCustomerEmail(authentication);
        model.addAttribute("order", orderService.findMyOrderDetailOrThrow(id, email));
        return "shop/my-order-detail";
    }
}