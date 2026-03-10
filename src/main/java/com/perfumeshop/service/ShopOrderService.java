package com.perfumeshop.service;

import com.perfumeshop.dto.ShopCheckoutForm;
import com.perfumeshop.entity.Order;
import com.perfumeshop.entity.OrderItem;
import com.perfumeshop.entity.Product;
import com.perfumeshop.enums.OrderStatus;
import com.perfumeshop.enums.PaymentMethod;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShopOrderService {

    private final ShopCartService cartService;
    private final ProductService productService;
    private final OrderService orderService;
    private final InvoiceService invoiceService;
    private final CustomerService customerService;
    private final BakongQrService bakongQrService;

    public ShopOrderService(ShopCartService cartService,
                            ProductService productService,
                            OrderService orderService,
                            InvoiceService invoiceService,
                            CustomerService customerService,
                            BakongQrService bakongQrService) {
        this.cartService = cartService;
        this.productService = productService;
        this.orderService = orderService;
        this.invoiceService = invoiceService;
        this.customerService = customerService;
        this.bakongQrService = bakongQrService;
    }

    @Transactional
    public Order placeOrder(ShopCheckoutForm form, HttpSession session, String customerEmail) {
        var cartItems = cartService.getItems(session);

        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Your cart is empty");
        }

        if (form.getFullName() == null || form.getFullName().isBlank()) {
            throw new RuntimeException("Full name is required");
        }

        if (form.getPhone() == null || form.getPhone().isBlank()) {
            throw new RuntimeException("Phone is required");
        }

        if (form.getAddress() == null || form.getAddress().isBlank()) {
            throw new RuntimeException("Address is required");
        }

        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(
                    (form.getPaymentMethod() == null ? "CASH" : form.getPaymentMethod().trim().toUpperCase())
            );
        } catch (Exception e) {
            paymentMethod = PaymentMethod.CASH;
        }

        var customer = customerService.findByEmailOrThrow(customerEmail);

        Order order = new Order();
        order.setCustomer(customer);
        order.setInvoice(invoiceService.nextInvoice());
        order.setCustomerName(form.getFullName().trim());
        order.setPhone(form.getPhone().trim());
        order.setAddress(form.getAddress().trim());
        order.setPaymentMethod(paymentMethod);
        order.setDiscount(BigDecimal.ZERO);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (var ci : cartItems) {
            Product product = productService.findOrThrow(ci.getProductId());

            int qty = ci.getQty() == null ? 0 : ci.getQty();
            if (qty <= 0) continue;

            int stock = product.getStock() == null ? 0 : product.getStock();
            if (stock < qty) {
                throw new RuntimeException("Not enough stock for: " + product.getName());
            }

            BigDecimal unitPrice = ci.getUnitPrice() == null ? BigDecimal.ZERO : ci.getUnitPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(product);
            oi.setQty(qty);
            oi.setUnitPrice(unitPrice.setScale(2, RoundingMode.HALF_UP));
            oi.setLineTotal(lineTotal.setScale(2, RoundingMode.HALF_UP));

            orderItems.add(oi);
            subtotal = subtotal.add(lineTotal);
        }

        if (orderItems.isEmpty()) {
            throw new RuntimeException("Your cart is empty");
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);
        order.setTotal(subtotal);
        order.setItems(orderItems);

        int totalItems = orderItems.stream()
                .mapToInt(OrderItem::getQty)
                .sum();
        order.setTotalItems(totalItems);

        if (paymentMethod == PaymentMethod.CASH) {
            for (OrderItem it : orderItems) {
                Product p = it.getProduct();
                int qty = it.getQty();

                int stock = p.getStock() == null ? 0 : p.getStock();
                if (stock < qty) {
                    throw new RuntimeException("Not enough stock for: " + p.getName());
                }

                p.setStock(stock - qty);
                productService.save(p);
            }

            order.setStatus(OrderStatus.PAID);
            Order saved = orderService.save(order);

            cartService.clear(session);
            return saved;
        }

        // KHQR flow
        order.setStatus(OrderStatus.PENDING);
        Order saved = orderService.save(order);

        BakongQrService.KhqrResult qr = bakongQrService.generate(saved.getTotal(), saved.getInvoice());
        saved.setKhqrString(qr.khqrString());
        saved.setMd5(qr.md5());

        saved = orderService.save(saved);

        return saved;
    }
}