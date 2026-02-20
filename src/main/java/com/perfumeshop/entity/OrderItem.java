// src/main/java/com/perfumeshop/entity/OrderItem.java
package com.perfumeshop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="product_id", nullable=false)
    private Product product;

    @Column(nullable=false)
    private Integer qty = 1;

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal price = BigDecimal.ZERO; // unit price snapshot

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    public void recalcLineTotal(){
        if(price==null) price = BigDecimal.ZERO;
        if(qty==null) qty = 0;
        lineTotal = price.multiply(BigDecimal.valueOf(qty));
    }

    // getters/setters ...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}