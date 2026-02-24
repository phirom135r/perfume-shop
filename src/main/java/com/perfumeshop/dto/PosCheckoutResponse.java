package com.perfumeshop.dto;

import java.math.BigDecimal;

public class PosCheckoutResponse {

    private Long orderId;
    private String redirectUrl;

    // for KHQR popup
    private String invoice;
    private BigDecimal amount;
    private String md5;
    private String khqrString;

    public PosCheckoutResponse() {}

    // CASH
    public PosCheckoutResponse(Long orderId, String redirectUrl) {
        this.orderId = orderId;
        this.redirectUrl = redirectUrl;
    }

    // KHQR popup
    public PosCheckoutResponse(Long orderId, String invoice, BigDecimal amount, String md5, String khqrString) {
        this.orderId = orderId;
        this.invoice = invoice;
        this.amount = amount;
        this.md5 = md5;
        this.khqrString = khqrString;
    }

    public Long getOrderId() { return orderId; }
    public String getRedirectUrl() { return redirectUrl; }
    public String getInvoice() { return invoice; }
    public BigDecimal getAmount() { return amount; }
    public String getMd5() { return md5; }
    public String getKhqrString() { return khqrString; }

    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
    public void setInvoice(String invoice) { this.invoice = invoice; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setMd5(String md5) { this.md5 = md5; }
    public void setKhqrString(String khqrString) { this.khqrString = khqrString; }
}