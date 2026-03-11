package com.perfumeshop.controller.admin;

import com.perfumeshop.dto.DataTableResponse;
import com.perfumeshop.dto.ProductRowDto;
import com.perfumeshop.entity.Product;
import com.perfumeshop.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/admin/api/products")
public class ProductApiController {

    private final ProductService service;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public ProductApiController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/pos")
    public Page<Map<String, Object>> posProducts(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by("id").descending()
        );

        Page<Product> result = service.search(q, categoryId, true, pageable);

        return result.map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("size", p.getSize() == null ? "" : p.getSize());

            m.put("brandId", p.getBrand() != null ? p.getBrand().getId() : null);
            m.put("brand", p.getBrand() != null ? p.getBrand().getName() : "");

            BigDecimal price = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
            BigDecimal discount = p.getDiscount() == null ? BigDecimal.ZERO : p.getDiscount();

            if (discount.compareTo(BigDecimal.ZERO) < 0) discount = BigDecimal.ZERO;
            if (discount.compareTo(price) > 0) discount = price;

            BigDecimal finalPrice = price.subtract(discount);

            m.put("price", price);
            m.put("discount", discount);
            m.put("finalPrice", finalPrice);
            m.put("stock", p.getStock() == null ? 0 : p.getStock());

            String img = p.getImage();
            if (img == null || img.isBlank()) {
                m.put("imageUrl", "/images/no-image.png");
            } else if (img.startsWith("http") || img.startsWith("/")) {
                m.put("imageUrl", img);
            } else {
                m.put("imageUrl", "/" + img.replace("\\", "/"));
            }

            return m;
        });
    }

    @GetMapping("/dt")
    public DataTableResponse<ProductRowDto> datatable(
            @RequestParam(defaultValue = "0") int draw,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int length,
            @RequestParam(name = "search[value]", defaultValue = "") String search,
            @RequestParam(name = "order[0][column]", defaultValue = "0") int orderCol,
            @RequestParam(name = "order[0][dir]", defaultValue = "asc") String orderDir,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active
    ) {
        int page = Math.max(0, start / Math.max(1, length));
        Sort sort = buildSort(orderCol, orderDir);
        Pageable pageable = PageRequest.of(page, length, sort);

        Page<Product> result = service.search(search, categoryId, active, pageable);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        List<ProductRowDto> rows = new ArrayList<>();

        for (Product p : result.getContent()) {
            ProductRowDto dto = new ProductRowDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setSize(p.getSize());
            dto.setBrand(p.getBrand() != null ? p.getBrand().getName() : "");
            dto.setBrandId(p.getBrand() != null ? p.getBrand().getId() : null);
            dto.setStock(p.getStock());
            dto.setPrice(p.getPrice());
            dto.setDiscount(p.getDiscount());
            dto.setImage(p.getImage());
            dto.setActive(p.getActive());
            dto.setCategory(p.getCategory() != null ? p.getCategory().getName() : "");
            dto.setCategoryId(p.getCategory() != null ? p.getCategory().getId() : null);
            dto.setCreatedAt(p.getCreatedAt() != null ? p.getCreatedAt().format(fmt) : "");
            rows.add(dto);
        }

        long total = service.countAll();
        long filtered = result.getTotalElements();

        return new DataTableResponse<>(draw, total, filtered, rows);
    }

    private Sort buildSort(int col, String dir) {
        boolean asc = !"desc".equalsIgnoreCase(dir);

        String prop;
        switch (col) {
            case 0 -> prop = "id";
            case 1 -> prop = "name";
            case 2 -> prop = "size";
            case 3 -> prop = "category.name";
            case 4 -> prop = "brand.name";
            case 5 -> prop = "stock";
            case 6 -> prop = "price";
            case 9 -> prop = "createdAt";
            default -> prop = "id";
        }

        return asc ? Sort.by(prop).ascending() : Sort.by(prop).descending();
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> save(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam String size,
            @RequestParam(required = false, defaultValue = "") String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false, defaultValue = "0") BigDecimal discount,
            @RequestParam Integer stock,
            @RequestParam Long categoryId,
            @RequestParam Long brandId,
            @RequestParam(defaultValue = "true") Boolean active,
            @RequestParam(required = false) MultipartFile image
    ) {
        try {
            Product p = (id == null) ? new Product() : service.findOrThrow(id);

            p.setName(name.trim());
            p.setSize(size.trim());
            p.setDescription(description.trim());
            p.setPrice(price);
            p.setDiscount(discount);
            p.setStock(stock);
            p.setActive(active);

            p.setCategory(service.findCategoryOrThrow(categoryId));
            p.setBrand(service.findBrandOrThrow(brandId));

            if (image != null && !image.isEmpty()) {
                Path dir = Paths.get(uploadDir, "products").toAbsolutePath().normalize();
                Files.createDirectories(dir);

                String original = image.getOriginalFilename() == null ? "image" : image.getOriginalFilename();
                String safeOriginal = original.replaceAll("[^a-zA-Z0-9._-]", "_");
                String fileName = UUID.randomUUID() + "_" + safeOriginal;

                Path target = dir.resolve(fileName);
                image.transferTo(target.toFile());

                p.setImage("/uploads/products/" + fileName);
            }

            return ResponseEntity.ok(service.save(p));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable Long id) {
        service.toggleActive(id);
        return ResponseEntity.ok().build();
    }
}