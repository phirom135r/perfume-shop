package com.perfumeshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderRowDto {

    private Long id;
    private String invoice;
    private String customerName;
    private String phone;
    private BigDecimal total;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;

    private Integer totalItems;

    public OrderRowDto(Long id,
                       String invoice,
                       String customerName,
                       String phone,
                       BigDecimal total,
                       String paymentMethod,
                       String status,
                       LocalDateTime createdAt,
                       Integer totalItems) {

        this.id = id;
        this.invoice = invoice;
        this.customerName = customerName;
        this.phone = phone;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
        this.totalItems = totalItems;
    }

    public Long getId() { return id; }
    public String getInvoice() { return invoice; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public BigDecimal getTotal() { return total; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Integer getTotalItems() { return totalItems; }
}