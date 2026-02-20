// src/main/java/com/perfumeshop/dto/OrderItemRequest.java
package com.perfumeshop.dto;

public class OrderItemRequest {
    private Long productId;
    private Integer qty;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
}