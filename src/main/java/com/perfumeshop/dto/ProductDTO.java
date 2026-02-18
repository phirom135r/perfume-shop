package com.perfumeshop.dto;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class ProductDTO {
    private Long id;
    private String name;
    private String brand;
    private Long categoryId;
    private String description;
    private BigDecimal price;
    private BigDecimal discount;
    private Integer stock;

    private MultipartFile imageFile; // upload
    private String existingImage;     // keep old

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public MultipartFile getImageFile() { return imageFile; }
    public void setImageFile(MultipartFile imageFile) { this.imageFile = imageFile; }

    public String getExistingImage() { return existingImage; }
    public void setExistingImage(String existingImage) { this.existingImage = existingImage; }
}