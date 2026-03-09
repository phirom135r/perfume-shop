package com.perfumeshop.controller.shop;

import com.perfumeshop.service.BrandService;
import com.perfumeshop.service.CategoryService;
import com.perfumeshop.service.ProductService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perfume-shop/shop")
public class ShopProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public ShopProductController(ProductService productService,
                                 CategoryService categoryService,
                                 BrandService brandService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    @GetMapping
    public String shopPage(@RequestParam(defaultValue = "") String q,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) Long brandId,
                           @RequestParam(defaultValue = "latest") String sort,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "12") int size,
                           Model model) {

        Sort sorting = switch (sort) {
            case "priceAsc" -> Sort.by("price").ascending();
            case "priceDesc" -> Sort.by("price").descending();
            case "nameAsc" -> Sort.by("name").ascending();
            default -> Sort.by("id").descending();
        };

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sorting);

        var products = productService.shopSearch(q, categoryId, brandId, pageable);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.listActive());
        model.addAttribute("brands", brandService.listActive());

        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("sort", sort);

        return "shop/shop";
    }
}