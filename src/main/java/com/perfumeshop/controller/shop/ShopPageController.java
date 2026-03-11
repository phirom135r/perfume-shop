package com.perfumeshop.controller.shop;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/perfume-shop")
public class ShopPageController {

    @GetMapping("/about")
    public String aboutPage() {
        return "shop/about";
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "shop/contact";
    }

}