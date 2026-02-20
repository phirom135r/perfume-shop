package com.perfumeshop.service;

import com.perfumeshop.dto.*;
import com.perfumeshop.entity.Order;
import com.perfumeshop.entity.OrderItem;
import com.perfumeshop.entity.Payment;
import com.perfumeshop.entity.Product;
import com.perfumeshop.enums.OrderStatus;
import com.perfumeshop.enums.PaymentMethod;
import com.perfumeshop.enums.PaymentStatus;
import com.perfumeshop.repository.OrderRepository;
import com.perfumeshop.repository.PaymentRepository;
import com.perfumeshop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final ProductRepository productRepo;
    private final OrderNumberService orderNoService;

    // TODO: Inject your KHQR service here later
    // private final KhqrService khqrService;

    public OrderService(OrderRepository orderRepo,
                        PaymentRepository paymentRepo,
                        ProductRepository productRepo,
                        OrderNumberService orderNoService) {
        this.orderRepo = orderRepo;
        this.paymentRepo = paymentRepo;
        this.productRepo = productRepo;
        this.orderNoService = orderNoService;
    }

    @Transactional
    public CheckoutResponse placePosOrder(PosCheckoutRequest req) {
        return placeOrderCommon(
                req.getCustomerName(),
                normalize(req.getPhone()),
                normalize(req.getAddress()), // optional
                req.getPaymentMethod(),
                req.getExtraDiscount(),
                req.getItems()
        );
    }

    @Transactional
    public CheckoutResponse placeWebOrder(WebCheckoutRequest req) {
        // address required by DTO validation already
        return placeOrderCommon(
                req.getCustomerName(),
                normalize(req.getPhone()),
                normalize(req.getAddress()), // required
                PaymentMethod.KHQR,          // website always KHQR (if you want)
                BigDecimal.ZERO,
                req.getItems()
        );
    }

    private CheckoutResponse placeOrderCommon(String customerName,
                                              String phone,
                                              String address,
                                              PaymentMethod paymentMethod,
                                              BigDecimal extraDiscount,
                                              java.util.List<OrderItemRequest> items) {

        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Cart is empty.");
        }

        Order order = new Order();
        order.setOrderNo(orderNoService.generate());
        order.setCustomerName(customerName.trim());
        order.setPhone(phone);
        order.setAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setCurrency("USD");

        BigDecimal subtotal = BigDecimal.ZERO;

        // create items + stock check
        for (OrderItemRequest it : items) {
            Product product = productRepo.findById(it.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + it.getProductId()));

            if (product.getStock() < it.getQty()) {
                throw new RuntimeException("Stock not enough for " + product.getName());
            }

            // reduce stock
            product.setStock(product.getStock() - it.getQty());
            productRepo.save(product);

            OrderItem oi = new OrderItem();
            oi.setProduct(product);
            oi.setProductNameSnapshot(product.getName());
            oi.setQty(it.getQty());
            oi.setUnitPrice(product.getPrice()); // BigDecimal
            oi.recalcLineTotal();

            subtotal = subtotal.add(oi.getLineTotal());
            order.addItem(oi);
        }

        BigDecimal discount = (extraDiscount == null ? BigDecimal.ZERO : extraDiscount);
        if (discount.compareTo(BigDecimal.ZERO) < 0) discount = BigDecimal.ZERO;
        if (discount.compareTo(subtotal) > 0) discount = subtotal;

        BigDecimal grandTotal = subtotal.subtract(discount);

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setGrandTotal(grandTotal);

        // payment
        if (paymentMethod == PaymentMethod.CASH) {
            order.setStatus(OrderStatus.COMPLETED);

            Payment pay = new Payment();
            pay.setOrder(order);
            pay.setMethod(PaymentMethod.CASH);
            pay.setStatus(PaymentStatus.PAID);
            pay.setAmount(grandTotal);

            // Save order first (cascade items)
            Order saved = orderRepo.save(order);
            pay.setOrder(saved);
            Payment savedPay = paymentRepo.save(pay);

            return new CheckoutResponse(saved.getOrderNo(), saved.getId(), savedPay.getId(),
                    savedPay.getStatus().name(), "/admin/orders"); // redirect to orders
        }

        // KHQR
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        Order saved = orderRepo.save(order);

        Payment pay = new Payment();
        pay.setOrder(saved);
        pay.setMethod(PaymentMethod.KHQR);
        pay.setStatus(PaymentStatus.PENDING);
        pay.setAmount(grandTotal);

        // TODO: generate KHQR String + md5 from SDK/your khqr service
        // Example:
        // KhqrResult r = khqrService.generate(grandTotal, saved.getOrderNo());
        // pay.setKhqrString(r.getKhqrString());
        // pay.setMd5(r.getMd5());

        Payment savedPay = paymentRepo.save(pay);

        return new CheckoutResponse(saved.getOrderNo(), saved.getId(), savedPay.getId(),
                savedPay.getStatus().name(), "/payments/khqr/" + savedPay.getId());
    }

    private String normalize(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}