package com.perfumeshop.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String brand;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double price;
    private Double discount;
    private Integer stock;

    private String image;

    // ===== Constructors =====
    public Product() {}

    public Product(Long id, String name, String brand,
                   Category category, String description,
                   Double price, Double discount,
                   Integer stock, String image) {

        this.id = id;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.stock = stock;
        this.image = image;
    }

    // ===== Getter / Setter =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}