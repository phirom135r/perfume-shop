package com.perfumeshop.dto;

import java.math.BigDecimal;

public class CartItemDto {
    private Long productId;
    private String name;
    private String image;
    private String size;

    // តម្លៃដើម
    private BigDecimal originalPrice;

    // តម្លៃក្រោយបញ្ចុះ
    private BigDecimal unitPrice;

    // discount ក្នុង 1 qty
    private BigDecimal discountAmount;

    private Integer qty;
    private Integer stock;

    public CartItemDto() {
    }

    public CartItemDto(Long productId,
                       String name,
                       String image,
                       String size,
                       BigDecimal originalPrice,
                       BigDecimal unitPrice,
                       BigDecimal discountAmount,
                       Integer qty,
                       Integer stock) {
        this.productId = productId;
        this.name = name;
        this.image = image;
        this.size = size;
        this.originalPrice = originalPrice;
        this.unitPrice = unitPrice;
        this.discountAmount = discountAmount;
        this.qty = qty;
        this.stock = stock;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSize() {
        return size == null ? "" : size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice == null ? BigDecimal.ZERO : originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice == null ? BigDecimal.ZERO : unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount == null ? BigDecimal.ZERO : discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Integer getQty() {
        return qty == null ? 0 : qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Integer getStock() {
        return stock == null ? 0 : stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    // តម្លៃសរុបក្រោយបញ្ចុះ
    public BigDecimal getLineTotal() {
        return getUnitPrice().multiply(BigDecimal.valueOf(getQty()));
    }

    // តម្លៃសរុបដើម
    public BigDecimal getOriginalLineTotal() {
        return getOriginalPrice().multiply(BigDecimal.valueOf(getQty()));
    }

    // discount សរុប
    public BigDecimal getLineDiscount() {
        return getDiscountAmount().multiply(BigDecimal.valueOf(getQty()));
    }
}