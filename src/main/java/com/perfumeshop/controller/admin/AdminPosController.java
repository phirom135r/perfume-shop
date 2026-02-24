package com.perfumeshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPosController {

    @GetMapping("/pos")
    public String pos() {
        return "admin/pos";
    }
}