package com.perfumeshop.controller.admin;

import com.perfumeshop.dto.ProductResponse;
import com.perfumeshop.entity.Product;
import com.perfumeshop.repository.CategoryRepository;
import com.perfumeshop.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/admin/api/products")
public class AdminProductApiController {

    private final ProductService service;
    private final CategoryRepository categoryRepo;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public AdminProductApiController(ProductService service, CategoryRepository categoryRepo) {
        this.service = service;
        this.categoryRepo = categoryRepo;
    }

    // ✅ list with search + pagination + filter + sort + active
    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false, defaultValue = "id_desc") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        Page<Product> result = service.search(q, categoryId, active, sort, page, size);

        // category map for name
        Map<Long, String> catMap = new HashMap<>();
        categoryRepo.findAll().forEach(c -> catMap.put(c.getId(), c.getName()));

        List<ProductResponse> items = new ArrayList<>();
        for (Product p : result.getContent()) {
            ProductResponse r = new ProductResponse();
            r.setId(p.getId());
            r.setName(p.getName());
            r.setBrand(p.getBrand());
            r.setPrice(p.getPrice());
            r.setDiscount(p.getDiscount());
            r.setStock(p.getStock());
            r.setImage(p.getImage());
            r.setCategoryId(p.getCategoryId());
            r.setCategoryName(catMap.getOrDefault(p.getCategoryId(), ""));
            r.setDescription(p.getDescription());
            r.setActive(p.getActive() != null ? p.getActive() : true);
            items.add(r);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("items", items);
        res.put("page", result.getNumber());
        res.put("size", result.getSize());
        res.put("totalPages", result.getTotalPages());
        res.put("totalElements", result.getTotalElements());
        return res;
    }

    // ✅ create/update (multipart)
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> save(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "") String brand,
            @RequestParam(required = false, defaultValue = "") String description,
            @RequestParam Double price,
            @RequestParam(required = false, defaultValue = "0") Double discount,
            @RequestParam Integer stock,
            @RequestParam Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) MultipartFile image
    ) {
        try {
            Product p = (id != null) ? service.find(id) : new Product();
            if (p == null) p = new Product();

            p.setName(name.trim());
            p.setBrand(brand.trim());
            p.setDescription(description.trim());
            p.setPrice(price);
            p.setDiscount(discount);
            p.setStock(stock);
            p.setCategoryId(categoryId);
            if (active != null) p.setActive(active);

            if (image != null && !image.isEmpty()) {
                Path dir = Paths.get(uploadDir, "products").toAbsolutePath().normalize();
                Files.createDirectories(dir);

                String original = (image.getOriginalFilename() == null) ? "image" : image.getOriginalFilename();
                String safe = original.replaceAll("[^a-zA-Z0-9._-]", "_");
                String fileName = UUID.randomUUID() + "_" + safe;

                Path target = dir.resolve(fileName);
                image.transferTo(target.toFile());

                p.setImage("/uploads/products/" + fileName);
            }

            return ResponseEntity.ok(service.save(p));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // ✅ toggle active/inactive
    @PatchMapping("/{id}/active")
    public ResponseEntity<?> setActive(@PathVariable Long id, @RequestParam boolean value) {
        Product p = service.find(id);
        if (p == null) return ResponseEntity.notFound().build();
        p.setActive(value);
        return ResponseEntity.ok(service.save(p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}