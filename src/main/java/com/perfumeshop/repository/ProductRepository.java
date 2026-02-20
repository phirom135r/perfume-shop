package com.perfumeshop.repository;

import com.perfumeshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
        select p from Product p
        join p.category c
        where (:categoryId is null or c.id = :categoryId)
          and (:active is null or p.active = :active)
          and (
                :kw is null or :kw = '' or
                lower(p.name) like lower(concat('%', :kw, '%')) or
                lower(p.brand) like lower(concat('%', :kw, '%')) or
                lower(c.name) like lower(concat('%', :kw, '%')) or
                str(p.id) like concat('%', :kw, '%') or
                str(p.stock) like concat('%', :kw, '%') or
                str(p.price) like concat('%', :kw, '%') or
                str(p.discount) like concat('%', :kw, '%')
          )
    """)
    Page<Product> search(
            @Param("kw") String kw,
            @Param("categoryId") Long categoryId,
            @Param("active") Boolean active,
            Pageable pageable
    );
   Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
