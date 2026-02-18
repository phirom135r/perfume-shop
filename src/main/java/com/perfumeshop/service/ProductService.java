package com.perfumeshop.service;

import com.perfumeshop.entity.Product;
import com.perfumeshop.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public Page<Product> search(String q, Long categoryId, Boolean active, String sort, int page, int size) {

        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (q != null && !q.trim().isEmpty()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), like),
                            cb.like(cb.lower(root.get("brand")), like)
                    )
            );
        }

        if (categoryId != null && categoryId > 0) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoryId"), categoryId));
        }

        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }

        Sort s = Sort.by("id").descending();
        if ("price_asc".equals(sort)) s = Sort.by("price").ascending();
        if ("price_desc".equals(sort)) s = Sort.by("price").descending();

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), s);
        return repo.findAll(spec, pageable);
    }

    public Product find(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Product save(Product p) {
        if (p.getActive() == null) p.setActive(true);
        return repo.save(p);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}