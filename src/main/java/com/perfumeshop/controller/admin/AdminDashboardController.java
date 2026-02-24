package com.perfumeshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @GetMapping
    public String page(Model model) {
        model.addAttribute("activeMenu", "dashboard");
        return "admin/dashboard";
    }
}