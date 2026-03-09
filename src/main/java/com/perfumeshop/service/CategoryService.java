package com.perfumeshop.service;

import com.perfumeshop.entity.Category;
import com.perfumeshop.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    // =============================
    // LIST ALL (Admin)
    // =============================
    public List<Category> list() {
        return repository.findAll();
    }

    // =============================
    // LIST ACTIVE ONLY (Shop dropdown)
    // =============================
    public List<Category> listActive() {
        return repository.findByActiveTrueOrderByNameAsc();
    }

    // =============================
    // FIND BY ID
    // =============================
    public Category find(Long id) {
        return repository.findById(id).orElse(null);
    }

    // =============================
    // FIND OR THROW
    // =============================
    public Category findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }

    // =============================
    // SAVE (CREATE / UPDATE)
    // =============================
    public Category save(Category c) {
        return repository.save(c);
    }

    // =============================
    // DELETE
    // =============================
    public void delete(Long id) {
        repository.deleteById(id);
    }

}