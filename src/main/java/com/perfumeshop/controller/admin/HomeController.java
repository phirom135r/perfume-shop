package com.perfumeshop.controller.admin;

import com.perfumeshop.repository.CategoryRepository;
import com.perfumeshop.service.OrderService;
import com.perfumeshop.dto.OrderRowDto;
import com.perfumeshop.enums.OrderStatus;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class HomeController {

    private final OrderService orderService;
    private final CategoryRepository categoryRepo;

    public HomeController(OrderService orderService, CategoryRepository categoryRepo) {
        this.orderService = orderService;
        this.categoryRepo = categoryRepo;
    }

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model){
        model.addAttribute("activeMenu","dashboard");
        return "admin/dashboard";
    }


    // Brand page
    @GetMapping("/brands")
    public String brands(){
        return "admin/brands";
    }


    // Category page
    @GetMapping("/categories")
    public String categories(){
        return "admin/categories";
    }

    // Product page
    @GetMapping("/products")
    public String products(Model model){
        model.addAttribute("categories",
                categoryRepo.findByActiveTrueOrderByNameAsc());
        return "admin/products";
    }

    // Orders page
    @GetMapping("/orders")
    public String orders(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model){

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<OrderRowDto> orders = orderService.adminSearchRows(q,status,pageable);

        model.addAttribute("orders",orders);
        model.addAttribute("q",q);
        model.addAttribute("status",status);
        model.addAttribute("allStatuses",OrderStatus.values());
        model.addAttribute("activeMenu","orders");

        return "admin/orders";
    }

    // POS
    @GetMapping("/pos")
    public String pos(){
        return "admin/pos";
    }

    // Stock
    @GetMapping("/stock")
    public String stock(){
        return "admin/stock";
    }

}