package com.perfumeshop.controller.admin;

import com.perfumeshop.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/products")
public class AdminProductPageController {

    @Autowired
    private CategoryRepository categoryRepo;

    @GetMapping
    public String page(Model model){

        model.addAttribute(
                "categories",
                categoryRepo.findAll()
        );

        return "admin/products";
    }
}