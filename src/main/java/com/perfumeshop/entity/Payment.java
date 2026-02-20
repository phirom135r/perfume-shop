// src/main/java/com/perfumeshop/entity/Payment.java
package com.perfumeshop.entity;

import com.perfumeshop.enums.PaymentMethod;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false, unique=true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private PaymentMethod method = PaymentMethod.CASH;

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(length=50)
    private String status; // INIT, PAID, FAILED (string ok)

    @Column(length=2000)
    private String khqr; // store KHQR payload (optional)

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        if(createdAt==null) createdAt = LocalDateTime.now();
        if(status==null) status = "INIT";
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

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKhqr() {
        return khqr;
    }

    public void setKhqr(String khqr) {
        this.khqr = khqr;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}