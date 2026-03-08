package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Brand;
import com.perfumeshop.service.BrandService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/brands")
public class BrandApiController {

    private final BrandService service;

    public BrandApiController(BrandService service) {
        this.service = service;
    }

    @GetMapping
    public List<Brand> list() {
        return service.list();
    }

    // ✅ for dropdown (only active)
    @GetMapping("/active")
    public List<Brand> listActive() {
        return service.listActive();
    }

    @GetMapping("/{id}")
    public Brand get(@PathVariable Long id) {
        return service.find(id);
    }

    @PostMapping
    public Brand save(@RequestBody Brand b) {
        return service.save(b);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}