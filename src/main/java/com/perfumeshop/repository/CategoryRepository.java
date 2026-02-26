package com.perfumeshop.repository;

import com.perfumeshop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ✅ for dropdown / filter: only active categories
    List<Category> findByActiveTrueOrderByNameAsc();
}