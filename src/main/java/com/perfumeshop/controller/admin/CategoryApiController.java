package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Category;
import com.perfumeshop.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/categories")
public class CategoryApiController {

    private final CategoryService service;

    public CategoryApiController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<Category> list() {
        return service.list();
    }

    @GetMapping("/active")
    public List<Category> listActive() {
        return service.listActive();
    }

    @GetMapping("/simple")
    public List<Map<String, Object>> simple() {
        return service.listActive().stream()
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    m.put("name", c.getName());
                    return m;
                })
                .toList();
    }

    @GetMapping("/{id}")
    public Category get(@PathVariable Long id) {
        return service.find(id);
    }

    @PostMapping
    public Category save(@RequestBody Category c) {
        if (c.getActive() == null) c.setActive(true);
        return service.save(c);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}