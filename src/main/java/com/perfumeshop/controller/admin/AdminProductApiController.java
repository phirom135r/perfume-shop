package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Product;
import com.perfumeshop.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/api/products")
public class AdminProductApiController {

    private final ProductService service;

    // from application.properties: app.upload.dir=uploads
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public AdminProductApiController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Product>> list() {
        return ResponseEntity.ok(service.list());
    }

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
            @RequestParam(required = false) MultipartFile image
    ) {
        try {
            // ===== find existing or create new =====
            Product p = (id != null) ? service.find(id) : null;
            if (p == null) p = new Product();

            // ===== set fields =====
            p.setName(name == null ? "" : name.trim());
            p.setBrand(brand == null ? "" : brand.trim());
            p.setDescription(description == null ? "" : description.trim());
            p.setPrice(price);
            p.setDiscount(discount);
            p.setStock(stock);
            p.setCategoryId(categoryId);

            // ✅ IMPORTANT FIX for MySQL error:
            // "Field 'active' doesn't have a default value"
            // If your Product has getActive/setActive, keep this.
            // If you DON'T have active field, comment these 3 lines and fix DB default instead.
            try {
                if (p.getId() == null && p.getActive() == null) {
                    p.setActive(true);
                }
            } catch (Exception ignored) {
                // in case your Product entity doesn't have active field yet
            }

            // ===== handle image upload (only if new file selected) =====
            if (image != null && !image.isEmpty()) {

                // /uploads/products
                Path dir = Paths.get(uploadDir, "products").toAbsolutePath().normalize();
                Files.createDirectories(dir);

                String original = (image.getOriginalFilename() == null) ? "image" : image.getOriginalFilename();
                String safeOriginal = original.replaceAll("[^a-zA-Z0-9._-]", "_");

                String fileName = UUID.randomUUID() + "_" + safeOriginal;
                Path target = dir.resolve(fileName);

                image.transferTo(target.toFile());

                // served by WebConfig mapping: /uploads/** -> file:uploads/**
                p.setImage("/uploads/products/" + fileName);
            }

            Product saved = service.save(p);
            return ResponseEntity.ok(saved);

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Cannot save product: " + ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Cannot delete product: " + ex.getMessage());
        }
    }
}