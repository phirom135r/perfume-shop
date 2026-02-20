package com.perfumeshop.repository;

import com.perfumeshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // POS products (only active products, search by name)
    @Query("""
        SELECT p FROM Product p
        WHERE p.active = true
          AND (:q IS NULL OR :q = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
    Page<Product> posProducts(@Param("q") String q, Pageable pageable);

    // keep your existing search(...) for DataTables
    @Query("""
        SELECT p FROM Product p
        WHERE (:kw IS NULL OR :kw = '' OR
               LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR
               LOWER(p.brand) LIKE LOWER(CONCAT('%', :kw, '%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:active IS NULL OR p.active = :active)
    """)
    Page<Product> search(@Param("kw") String kw,
                         @Param("categoryId") Long categoryId,
                         @Param("active") Boolean active,
                         Pageable pageable);
}