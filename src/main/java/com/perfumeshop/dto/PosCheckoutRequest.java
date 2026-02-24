package com.perfumeshop.dto;

import java.util.List;

public class PosCheckoutRequest {

    private String customerName;
    private String phone;
    private String address;
    private String paymentMethod; // "CASH" | "KHQR"
    private double discount;      // keep as double in request (UI input)
    private List<Item> items;

    public static class Item {
        private Long productId;
        private int qty;

        public Long getProductId() { return productId; }
        public int getQty() { return qty; }

        public void setProductId(Long productId) { this.productId = productId; }
        public void setQty(int qty) { this.qty = qty; }
    }

    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getDiscount() { return discount; }
    public List<Item> getItems() { return items; }

    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setDiscount(double discount) { this.discount = discount; }
    public void setItems(List<Item> items) { this.items = items; }
}