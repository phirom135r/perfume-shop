package com.perfumeshop.dto;

public class CheckoutResponse {
    private String orderNo;
    private Long orderId;
    private Long paymentId;      // may be null for CASH
    private String paymentStatus;
    private String redirectUrl;  // if KHQR => "/payments/khqr/{paymentId}"

    public CheckoutResponse() {}

    public CheckoutResponse(String orderNo, Long orderId, Long paymentId, String paymentStatus, String redirectUrl) {
        this.orderNo = orderNo;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
        this.redirectUrl = redirectUrl;
    }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
}