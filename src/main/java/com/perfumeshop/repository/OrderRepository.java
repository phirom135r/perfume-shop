package com.perfumeshop.repository;

import com.perfumeshop.dto.OrderRowDto;
import com.perfumeshop.entity.Order;
import com.perfumeshop.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByMd5(String md5);

    // =====================================================
    // ADMIN SEARCH (normal page search)
    // =====================================================
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

    // =====================================================
    // ADMIN SEARCH ROWS (for DataTable)
    // =====================================================
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
            o.totalItems
        )
        from Order o
        where (
            :q is null or :q = '' or
            lower(o.invoice) like lower(concat('%', :q, '%')) or
            lower(o.customerName) like lower(concat('%', :q, '%')) or
            lower(coalesce(o.phone, '')) like lower(concat('%', :q, '%'))
        )
        and (:status is null or o.status = :status)
    """)
    Page<OrderRowDto> adminSearchRows(@Param("q") String q,
                                      @Param("status") OrderStatus status,
                                      Pageable pageable);
    // =====================================================
    // ORDER DETAIL WITH ITEMS + PRODUCT
    // =====================================================
    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findWithItemsById(@Param("id") Long id);

    // =====================================================
    // DASHBOARD - COUNTS BY STATUS IN RANGE
    // =====================================================
    @Query("""
        select count(o)
        from Order o
        where o.status = :status
          and o.createdAt >= :start
          and o.createdAt < :end
    """)
    long countByStatusBetween(@Param("status") OrderStatus status,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    // =====================================================
    // DASHBOARD - SUM PAID TOTAL IN RANGE
    // =====================================================
    @Query("""
        select coalesce(sum(o.total), 0)
        from Order o
        where o.status = :paid
          and o.createdAt >= :start
          and o.createdAt < :end
    """)
    BigDecimal sumTotalBetween(@Param("paid") OrderStatus paid,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    // =====================================================
    // DASHBOARD - COUNT PAID ORDERS IN RANGE
    // =====================================================
    @Query("""
        select count(o)
        from Order o
        where o.status = :paid
          and o.createdAt >= :start
          and o.createdAt < :end
    """)
    long countPaidBetween(@Param("paid") OrderStatus paid,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);

    // =====================================================
    // DASHBOARD - RECENT ORDERS IN RANGE
    // =====================================================
    @Query("""
        select o
        from Order o
        where o.createdAt >= :start
          and o.createdAt < :end
        order by o.createdAt desc
    """)
    List<Order> findRecentBetween(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  Pageable pageable);

    // =====================================================
    // DASHBOARD - DAILY REVENUE
    // MySQL date(o.createdAt)
    // =====================================================
    @Query("""
        select function('date', o.createdAt), coalesce(sum(o.total), 0)
        from Order o
        where o.status = :paid
          and o.createdAt >= :start
          and o.createdAt < :end
        group by function('date', o.createdAt)
        order by function('date', o.createdAt)
    """)
    List<Object[]> dailyRevenue(@Param("paid") OrderStatus paid,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    @Query("""
    select o
    from Order o
    where o.customer.email = :email
    order by o.createdAt desc
""")
    List<Order> findByCustomerEmailOrderByCreatedAtDesc(@Param("email") String email);

    @Query("""
    select o
    from Order o
    left join fetch o.items oi
    left join fetch oi.product
    where o.id = :id and o.customer.email = :email
""")
    Optional<Order> findDetailByIdAndCustomerEmail(@Param("id") Long id,
                                                   @Param("email") String email);
}