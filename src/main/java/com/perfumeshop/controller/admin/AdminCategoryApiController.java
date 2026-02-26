package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Category;
import com.perfumeshop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/categories")
public class AdminCategoryApiController {

    @Autowired
    private CategoryService service;

    // ✅ list all (admin table)
    @GetMapping
    public List<Category> list() {
        return service.list();
    }

    // ✅ list active only (for product dropdown)
    @GetMapping("/active")
    public List<Category> listActive() {
        return service.listActive();
    }

    @GetMapping("/{id}")
    public Category get(@PathVariable Long id) {
        return service.find(id);
    }

    @PostMapping
    public Category save(@RequestBody Category c) {
        // ✅ important: if active is null, default true
        if (c.getActive() == null) c.setActive(true);
        return service.save(c);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}