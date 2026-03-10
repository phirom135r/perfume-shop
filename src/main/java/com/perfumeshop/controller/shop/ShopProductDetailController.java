package com.perfumeshop.controller.shop;

import com.perfumeshop.entity.Product;
import com.perfumeshop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/perfume-shop/product")
public class ShopProductDetailController {

    private final ProductService productService;

    public ShopProductDetailController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Product product = productService.findActiveDetailOrThrow(id);

        List<Product> related = Collections.emptyList();
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            related = productService.relatedProducts(product.getCategory().getId(), product.getId());
        }

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", related);
        return "shop/product-detail";
    }
}