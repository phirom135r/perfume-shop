// src/main/java/com/perfumeshop/dto/CheckoutRequest.java
package com.perfumeshop.dto;

import java.math.BigDecimal;
import java.util.List;

public class CheckoutRequest {
    private String customerName;
    private String phone;
    private String address;
    private String paymentMethod; // CASH / KHQR
    private BigDecimal extraDiscount;
    private List<OrderItemRequest> items;

    // getters/setters...

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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getExtraDiscount() {
        return extraDiscount;
    }

    public void setExtraDiscount(BigDecimal extraDiscount) {
        this.extraDiscount = extraDiscount;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}