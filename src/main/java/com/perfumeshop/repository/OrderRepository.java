package com.perfumeshop.repository;

import com.perfumeshop.dto.OrderRowDto;
import com.perfumeshop.entity.Order;
import com.perfumeshop.enums.OrderStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByMd5(String md5);

    @Query("""
        select o
        from Order o
        where (
            :q is null or :q = '' or
            lower(o.invoice) like lower(concat('%', :q, '%')) or
            lower(o.customerName) like lower(concat('%', :q, '%')) or
            lower(coalesce(o.phone, '')) like lower(concat('%', :q, '%'))
        )
        and (:status is null or o.status = :status)
    """)
    Page<Order> adminSearch(@Param("q") String q,
                            @Param("status") OrderStatus status,
                            Pageable pageable);

    @Query("""
    select new com.perfumeshop.dto.OrderRowDto(
        o.id,
        o.invoice,
        o.customerName,
        o.phone,
        o.total,
        cast(o.paymentMethod as string),
        cast(o.status as string),
        o.createdAt,
        coalesce(sum(oi.qty), 0)
    )
    from Order o
    left join o.items oi
    where (
        :q is null or :q = '' or
        lower(o.invoice) like lower(concat('%', :q, '%')) or
        lower(o.customerName) like lower(concat('%', :q, '%')) or
        lower(coalesce(o.phone, '')) like lower(concat('%', :q, '%'))
    )
    and (:status is null or o.status = :status)
    group by o.id, o.invoice, o.customerName, o.phone,
             o.total, o.paymentMethod, o.status, o.createdAt
""")
    Page<OrderRowDto> adminSearchRows(@Param("q") String q,
                                      @Param("status") OrderStatus status,
                                      Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findWithItemsById(@Param("id") Long id);



    // ================= DASHBOARD (FILTER BY RANGE) =================

    @Query("""
        select count(o)
        from Order o
        where o.status = :status
          and o.createdAt >= :start
          and o.createdAt <  :end
    """)
    long countByStatusBetween(@Param("status") OrderStatus status,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    @Query("""
        select coalesce(sum(o.total), 0)
        from Order o
        where o.status = :paid
          and o.createdAt >= :start
          and o.createdAt <  :end
    """)
    BigDecimal sumTotalBetween(@Param("paid") OrderStatus paid,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    @Query("""
        select count(o)
        from Order o
        where o.status = :paid
          and o.createdAt >= :start
          and o.createdAt <  :end
    """)
    long countPaidBetween(@Param("paid") OrderStatus paid,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);

    @Query("""
        select o
        from Order o
        where o.createdAt >= :start
          and o.createdAt <  :end
        order by o.createdAt desc
    """)
    List<Order> findRecentBetween(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  Pageable pageable);

    // MySQL: date(createdAt)
    @Query("""
        select function('date', o.createdAt), coalesce(sum(o.total), 0)
        from Order o
        where o.status = :paid
          and o.createdAt >= :start
          and o.createdAt <  :end
        group by function('date', o.createdAt)
        order by function('date', o.createdAt)
    """)
    List<Object[]> dailyRevenue(@Param("paid") OrderStatus paid,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);



}