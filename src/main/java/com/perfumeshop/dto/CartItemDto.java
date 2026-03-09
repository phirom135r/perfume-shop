package com.perfumeshop.dto;

import java.math.BigDecimal;

public class CartItemDto {
    private Long productId;
    private String name;
    private String image;
    private String size;
    private BigDecimal unitPrice;
    private Integer qty;
    private Integer stock;

    public CartItemDto() {
    }

    public CartItemDto(Long productId, String name, String image, String size,
                       BigDecimal unitPrice, Integer qty, Integer stock) {
        this.productId = productId;
        this.name = name;
        this.image = image;
        this.size = size;
        this.unitPrice = unitPrice;
        this.qty = qty;
        this.stock = stock;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public BigDecimal getLineTotal() {
        BigDecimal price = unitPrice == null ? BigDecimal.ZERO : unitPrice;
        int q = qty == null ? 0 : qty;
        return price.multiply(BigDecimal.valueOf(q));
    }
}