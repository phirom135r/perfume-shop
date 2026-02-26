package com.perfumeshop.repository;

import com.perfumeshop.entity.OrderItem;
import com.perfumeshop.enums.OrderStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
        select p.name,
               sum(oi.qty),
               coalesce(sum(oi.lineTotal), 0)
        from OrderItem oi
        join oi.order o
        join oi.product p
        where o.status = :paid
          and o.createdAt >= :start
          and o.createdAt <  :end
        group by p.name
        order by coalesce(sum(oi.lineTotal), 0) desc
    """)
    List<Object[]> topProducts(@Param("paid") OrderStatus paid,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);
}