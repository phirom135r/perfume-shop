// src/main/java/com/perfumeshop/service/OrderService.java
package com.perfumeshop.service;

import com.perfumeshop.dto.CheckoutRequest;
import com.perfumeshop.dto.OrderItemRequest;
import com.perfumeshop.entity.*;
import com.perfumeshop.enums.*;
import com.perfumeshop.repository.OrderRepository;
import com.perfumeshop.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepo, PaymentRepository paymentRepo, ProductService productService) {
        this.orderRepo = orderRepo;
        this.paymentRepo = paymentRepo;
        this.productService = productService;
    }

    private String genOrderNo(){
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD" + ts;
    }

    @Transactional
    public Order createPosOrder(CheckoutRequest req){
        if(req.getItems()==null || req.getItems().isEmpty()){
            throw new RuntimeException("Cart is empty");
        }
        if(req.getCustomerName()==null || req.getCustomerName().trim().isEmpty()){
            throw new RuntimeException("Customer name is required");
        }

        PaymentMethod method = PaymentMethod.valueOf(req.getPaymentMethod());

        Order order = new Order();
        order.setOrderNo(genOrderNo());
        order.setSource(OrderSource.POS);
        order.setStatus(OrderStatus.PENDING);
        order.setCustomerName(req.getCustomerName().trim());
        order.setPhone(req.getPhone()==null ? null : req.getPhone().trim());
        order.setAddress(req.getAddress()==null ? null : req.getAddress().trim());

        BigDecimal subtotal = BigDecimal.ZERO;

        for(OrderItemRequest it : req.getItems()){
            Product p = productService.findOrThrow(it.getProductId());

            int qty = (it.getQty()==null) ? 0 : it.getQty();
            if(qty <= 0) throw new RuntimeException("Invalid qty for product " + p.getId());
            if(p.getStock() < qty) throw new RuntimeException("Stock not enough for: " + p.getName());

            // decrease stock
            p.setStock(p.getStock() - qty);

            OrderItem oi = new OrderItem();
            oi.setProduct(p);
            oi.setQty(qty);

            // ✅ snapshot price (USD)
            oi.setPrice(p.getPrice());
            oi.recalcLineTotal();

            subtotal = subtotal.add(oi.getLineTotal());
            order.addItem(oi);
        }

        BigDecimal extraDiscount = (req.getExtraDiscount()==null) ? BigDecimal.ZERO : req.getExtraDiscount();
        if(extraDiscount.compareTo(BigDecimal.ZERO) < 0) extraDiscount = BigDecimal.ZERO;
        if(extraDiscount.compareTo(subtotal) > 0) extraDiscount = subtotal;

        order.setSubtotal(subtotal);
        order.setExtraDiscount(extraDiscount);
        order.setTotal(subtotal.subtract(extraDiscount));

        // Payment record
        Payment pay = new Payment();
        pay.setOrder(order);
        pay.setMethod(method);
        pay.setAmount(order.getTotal());

        if(method == PaymentMethod.CASH){
            pay.setStatus("PAID");
            order.setStatus(OrderStatus.COMPLETED);
        } else {
            pay.setStatus("INIT");
            // TODO: integrate Bakong KHQR later and set pay.setKhqr(...)
            order.setStatus(OrderStatus.PENDING);
        }

        order.setPayment(pay);

        return orderRepo.save(order); // cascade saves items + payment
    }
}