// src/main/java/com/perfumeshop/controller/admin/AdminProductPosApiController.java
package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Product;
import com.perfumeshop.service.ProductService;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin/api/products")
public class AdminProductPosApiController {

    private final ProductService service;

    public AdminProductPosApiController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/pos")
    public Map<String, Object> posProducts(
            @RequestParam(defaultValue="") String q,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="9") int size
    ){
        Page<Product> p = service.posProducts(q, PageRequest.of(page, size, Sort.by("id").descending()));

        List<Map<String,Object>> items = new ArrayList<>();
        for(Product x: p.getContent()){
            Map<String,Object> row = new HashMap<>();
            row.put("id", x.getId());
            row.put("name", x.getName());
            row.put("brand", x.getBrand());
            row.put("price", x.getPrice());   // BigDecimal
            row.put("image", x.getImage());
            row.put("stock", x.getStock());
            items.add(row);
        }

        Map<String,Object> res = new HashMap<>();
        res.put("content", items);
        res.put("page", p.getNumber());
        res.put("totalPages", p.getTotalPages());
        res.put("totalElements", p.getTotalElements());
        return res;
    }
}