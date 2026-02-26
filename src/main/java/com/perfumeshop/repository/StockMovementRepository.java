package com.perfumeshop.repository;

import com.perfumeshop.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("""
    SELECT sm FROM StockMovement sm
    JOIN sm.product p
    WHERE (:kw IS NULL OR :kw = '' OR
           CAST(sm.id as string) LIKE CONCAT('%', :kw, '%') OR
           LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')))
""")
    Page<StockMovement> search(@Param("kw") String kw, Pageable pageable);
}