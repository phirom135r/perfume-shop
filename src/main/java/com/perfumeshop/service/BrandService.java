package com.perfumeshop.service;

import com.perfumeshop.entity.Brand;
import com.perfumeshop.repository.BrandRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService {

    private final BrandRepository repo;

    public BrandService(BrandRepository repo) {
        this.repo = repo;
    }

    public List<Brand> list() { return repo.findAll(); }

    public List<Brand> listActive() { return repo.findByActiveTrueOrderByNameAsc(); }

    public Brand find(Long id) { return repo.findById(id).orElse(null); }

    public Brand findOrThrow(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Brand not found: " + id));
    }

    public Brand save(Brand b) { return repo.save(b); }

    public void delete(Long id) { repo.deleteById(id); }
}