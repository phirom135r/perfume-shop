package com.perfumeshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StockPageController {

    @GetMapping("/admin/stock")
    public String stockPage() {
        return "admin/stock";
    }
}