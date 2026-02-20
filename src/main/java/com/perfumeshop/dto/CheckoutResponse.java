// src/main/java/com/perfumeshop/dto/CheckoutResponse.java
package com.perfumeshop.dto;

public class CheckoutResponse {
    private String orderNo;
    private String redirectUrl; // when KHQR

    public CheckoutResponse() {}
    public CheckoutResponse(String orderNo, String redirectUrl){
        this.orderNo = orderNo; this.redirectUrl = redirectUrl;
    }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
}