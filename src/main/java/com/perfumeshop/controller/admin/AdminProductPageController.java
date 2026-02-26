package com.perfumeshop.controller.admin;

import com.perfumeshop.repository.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/products")
public class AdminProductPageController {

    private final CategoryRepository categoryRepo;

    public AdminProductPageController(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @GetMapping
    public String page(Model model) {
        // ✅ only active categories for dropdown/filter
        model.addAttribute("categories", categoryRepo.findByActiveTrueOrderByNameAsc());
        return "admin/products/index";
    }
}