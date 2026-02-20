package com.perfumeshop.dto;

import com.perfumeshop.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class PosCheckoutRequest {

    @NotBlank
    private String customerName;

    private String phone;     // optional
    private String address;   // optional (POS)

    @NotNull
    private PaymentMethod paymentMethod;

    // optional extra discount for POS
    private BigDecimal extraDiscount = BigDecimal.ZERO;

    @Valid
    @NotEmpty
    private List<OrderItemRequest> items;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getExtraDiscount() { return extraDiscount == null ? BigDecimal.ZERO : extraDiscount; }
    public void setExtraDiscount(BigDecimal extraDiscount) { this.extraDiscount = extraDiscount; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}