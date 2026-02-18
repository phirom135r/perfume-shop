package com.perfumeshop.service;

import com.perfumeshop.entity.Category;
import com.perfumeshop.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository repository;

    // ===== List =====
    public List<Category> list() {
        return repository.findAll();
    }

    // ===== Find =====
    public Category find(Long id) {
        return repository.findById(id).orElse(null);
    }

    // ===== Save =====
    public Category save(Category c) {
        return repository.save(c);
    }

    // ===== Delete =====
    public void delete(Long id) {
        repository.deleteById(id);
    }
}