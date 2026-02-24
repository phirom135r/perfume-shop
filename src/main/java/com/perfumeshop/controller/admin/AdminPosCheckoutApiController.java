package com.perfumeshop.controller.admin;

import com.perfumeshop.dto.PosCheckoutRequest;
import com.perfumeshop.dto.PosCheckoutResponse;
import com.perfumeshop.entity.Order;
import com.perfumeshop.entity.OrderItem;
import com.perfumeshop.entity.Product;
import com.perfumeshop.enums.OrderStatus;
import com.perfumeshop.enums.PaymentMethod;
import com.perfumeshop.service.BakongQrService;
import com.perfumeshop.service.InvoiceService;
import com.perfumeshop.service.OrderService;
import com.perfumeshop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/pos")
public class AdminPosCheckoutApiController {

    private final OrderService orderService;
    private final ProductService productService;
    private final BakongQrService bakongQrService;
    private final InvoiceService invoiceService;

    public AdminPosCheckoutApiController(
            OrderService orderService,
            ProductService productService,
            BakongQrService bakongQrService,
            InvoiceService invoiceService
    ) {
        this.orderService = orderService;
        this.productService = productService;
        this.bakongQrService = bakongQrService;
        this.invoiceService = invoiceService;
    }

    @PostMapping("/checkout")
    @Transactional
    public ResponseEntity<?> checkout(@RequestBody PosCheckoutRequest req) {

        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty");
        }

        String customerName = (req.getCustomerName() == null) ? "" : req.getCustomerName().trim();
        if (customerName.isBlank()) {
            return ResponseEntity.badRequest().body("Customer name is required");
        }

        PaymentMethod pm = (req.getPaymentMethod() == null || req.getPaymentMethod().isBlank())
                ? PaymentMethod.CASH
                : PaymentMethod.valueOf(req.getPaymentMethod().trim().toUpperCase());

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (PosCheckoutRequest.Item it : req.getItems()) {
            if (it == null || it.getProductId() == null || it.getQty() <= 0) continue;

            Product p = productService.findOrThrow(it.getProductId());
            int qty = it.getQty();

            if (p.getStock() != null && p.getStock() < qty) {
                return ResponseEntity.badRequest().body("Not enough stock for product: " + p.getName());
            }

            BigDecimal unitPrice = (p.getPrice() == null) ? BigDecimal.ZERO : p.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            subtotal = subtotal.add(lineTotal);

            OrderItem oi = new OrderItem();
            oi.setProduct(p);
            oi.setQty(qty);
            // NOTE: only set if your entity has these fields+setter
             oi.setUnitPrice(unitPrice);
             oi.setLineTotal(lineTotal);

            items.add(oi);

            if (p.getStock() != null) {
                p.setStock(p.getStock() - qty);
                productService.save(p);
            }
        }

        if (items.isEmpty()) return ResponseEntity.badRequest().body("Cart is empty");

        BigDecimal discount = BigDecimal.valueOf(req.getDiscount());
        if (discount.compareTo(BigDecimal.ZERO) < 0) discount = BigDecimal.ZERO;
        if (discount.compareTo(subtotal) > 0) discount = subtotal;

        BigDecimal total = subtotal.subtract(discount);

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        discount = discount.setScale(2, RoundingMode.HALF_UP);
        total = total.setScale(2, RoundingMode.HALF_UP);

        Order order = new Order();
        order.setInvoice(invoiceService.nextInvoice()); // INV-000001
        order.setCustomerName(customerName);
        order.setPhone(req.getPhone());
        order.setAddress(req.getAddress());
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setTotal(total);
        order.setPaymentMethod(pm);

        for (OrderItem oi : items) oi.setOrder(order);
        order.setItems(items);

        if (pm == PaymentMethod.CASH) {
            order.setStatus(OrderStatus.PAID); // ✅ Completed in UI
            order = orderService.save(order);
            return ResponseEntity.ok(new PosCheckoutResponse(order.getId(), "/admin/orders"));
        }

        // KHQR
        order.setStatus(OrderStatus.PENDING);
        order = orderService.save(order);

        BakongQrService.KhqrResult qr = bakongQrService.generate(order.getTotal(), order.getInvoice());

        order.setKhqrString(qr.khqrString());
        order.setMd5(qr.md5());
        order = orderService.save(order);

        // RETURN popup data (NO redirectUrl)
        return ResponseEntity.ok(
                new PosCheckoutResponse(order.getId(), order.getInvoice(), order.getTotal(), order.getMd5(), order.getKhqrString())
        );
    }
}