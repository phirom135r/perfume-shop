package com.perfumeshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/pos")
public class AdminPosPageController {

    @GetMapping
    public String page() {
        return "admin/pos";
    }
}