package com.perfumeshop.service;

import com.perfumeshop.entity.Brand;
import com.perfumeshop.entity.Category;
import com.perfumeshop.entity.Product;
import com.perfumeshop.repository.BrandRepository;
import com.perfumeshop.repository.CategoryRepository;
import com.perfumeshop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;

    public ProductService(ProductRepository productRepo,
                          CategoryRepository categoryRepo,
                          BrandRepository brandRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.brandRepo = brandRepo;
    }

    public long countAll() {
        return productRepo.count();
    }

    public Page<Product> search(String kw, Long categoryId, Boolean active, Pageable pageable) {
        return productRepo.search(kw, categoryId, active, pageable);
    }

    // ✅ POS products for cards (admin POS)
    public Page<Product> posProducts(String q, Pageable pageable) {
        String kw = (q == null) ? "" : q.trim();
        return productRepo.posProducts(kw, pageable);
    }

    public Product find(Long id) {
        return productRepo.findById(id).orElse(null);
    }

    public Product findOrThrow(Long id) {
        Optional<Product> opt = productRepo.findById(id);
        return opt.orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    public Category findCategoryOrThrow(Long id) {
        Category c = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));

        if (!Boolean.TRUE.equals(c.getActive())) {
            throw new RuntimeException("Category is inactive: " + c.getName());
        }
        return c;
    }

    public Brand findBrandOrThrow(Long id) {
        Brand b = brandRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found: " + id));

        // ✅ If Brand has active status like Category
        // (If your Brand entity doesn't have getActive(), remove this block)
        if (b.getActive() != null && !Boolean.TRUE.equals(b.getActive())) {
            throw new RuntimeException("Brand is inactive: " + b.getName());
        }

        return b;
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
}