package com.perfumeshop.service;

import com.perfumeshop.entity.Brand;
import com.perfumeshop.repository.BrandRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService {

    private final BrandRepository repository;

    public BrandService(BrandRepository repository) {
        this.repository = repository;
    }

    // =============================
    // LIST ALL (Admin)
    // =============================
    public List<Brand> list() {
        return repository.findAll();
    }

    // =============================
    // LIST ACTIVE ONLY (Shop dropdown)
    // =============================
    public List<Brand> listActive() {
        return repository.findByActiveTrueOrderByNameAsc();
    }

    // =============================
    // FIND BY ID
    // =============================
    public Brand find(Long id) {
        return repository.findById(id).orElse(null);
    }

    // =============================
    // FIND OR THROW
    // =============================
    public Brand findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found: " + id));
    }

    // =============================
    // SAVE (CREATE / UPDATE)
    // =============================
    public Brand save(Brand b) {
        return repository.save(b);
    }

    // =============================
    // DELETE
    // =============================
    public void delete(Long id) {
        repository.deleteById(id);
    }

}