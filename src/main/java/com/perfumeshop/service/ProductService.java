package com.perfumeshop.service;

import com.perfumeshop.entity.Category;
import com.perfumeshop.entity.Product;
import com.perfumeshop.repository.CategoryRepository;
import com.perfumeshop.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    public ProductService(ProductRepository productRepo, CategoryRepository categoryRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    public long countAll() {
        return productRepo.count();
    }

    public Page<Product> search(String kw, Long categoryId, Boolean active, Pageable pageable) {
        return productRepo.search(kw, categoryId, active, pageable);
    }

    // ✅ NEW: For POS card (Ajax)
    public Page<Product> searchPos(String q, Pageable pageable) {
        String kw = (q == null) ? "" : q.trim();
        if (kw.isEmpty()) {
            // show only active products in POS
            return productRepo.findByActiveTrue(pageable);
        }
        return productRepo.findByActiveTrueAndNameContainingIgnoreCase(kw, pageable);
    }

    // ✅ NEW: For cart/shop usage
    public Product find(Long id) {
        return productRepo.findById(id).orElse(null);
    }

    public Product findOrThrow(Long id) {
        Optional<Product> opt = productRepo.findById(id);
        return opt.orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    public Category findCategoryOrThrow(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }

    public Product save(Product p) {
        return productRepo.save(p);
    }

    public void delete(Long id) {
        productRepo.deleteById(id);
    }

    public void toggleActive(Long id) {
        Product p = findOrThrow(id);
        p.setActive(!Boolean.TRUE.equals(p.getActive()));
        productRepo.save(p);
    }
    public Page<Product> posProducts(String q, Pageable pageable) {
        String kw = (q == null) ? "" : q.trim();
        if (kw.isEmpty()) {
            return productRepo.findByActiveTrue(pageable);
        }
        return productRepo.findByActiveTrueAndNameContainingIgnoreCase(kw, pageable);
    }
}