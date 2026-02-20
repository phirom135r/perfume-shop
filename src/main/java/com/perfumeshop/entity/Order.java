// src/main/java/com/perfumeshop/entity/Order.java
package com.perfumeshop.entity;

import com.perfumeshop.enums.OrderSource;
import com.perfumeshop.enums.OrderStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_no", nullable=false, unique=true, length=30)
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private OrderSource source = OrderSource.POS;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable=false, length=150)
    private String customerName;

    @Column(length=30)
    private String phone;

    @Column(length=255)
    private String address; // POS optional, Website required

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal extraDiscount = BigDecimal.ZERO;

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true)
    private Payment payment;

    @PrePersist
    public void prePersist(){
        if(createdAt==null) createdAt = LocalDateTime.now();
    }

    public void addItem(OrderItem item){
        items.add(item);
        item.setOrder(this);
    }

    // getters/setters ...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public OrderSource getSource() {
        return source;
    }

    public void setSource(OrderSource source) {
        this.source = source;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getExtraDiscount() {
        return extraDiscount;
    }

    public void setExtraDiscount(BigDecimal extraDiscount) {
        this.extraDiscount = extraDiscount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}