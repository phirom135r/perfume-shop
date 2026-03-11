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
public class PosCheckoutApiController {

    private final OrderService orderService;
    private final ProductService productService;
    private final BakongQrService bakongQrService;
    private final InvoiceService invoiceService;

    public PosCheckoutApiController(
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

        String customerName = req.getCustomerName() == null ? "" : req.getCustomerName().trim();
        if (customerName.isBlank()) {
            return ResponseEntity.badRequest().body("Customer name is required");
        }

        PaymentMethod pm;
        try {
            pm = (req.getPaymentMethod() == null || req.getPaymentMethod().isBlank())
                    ? PaymentMethod.CASH
                    : PaymentMethod.valueOf(req.getPaymentMethod().trim().toUpperCase());
        } catch (Exception e) {
            pm = PaymentMethod.CASH;
        }

        BigDecimal originalSubtotal = BigDecimal.ZERO;   // តម្លៃដើមសរុប
        BigDecimal productDiscount = BigDecimal.ZERO;    // discount ពី product
        List<OrderItem> items = new ArrayList<>();

        for (PosCheckoutRequest.Item it : req.getItems()) {
            if (it == null || it.getProductId() == null || it.getQty() <= 0) continue;

            Product p = productService.findOrThrow(it.getProductId());
            int qty = it.getQty();

            int stock = p.getStock() == null ? 0 : p.getStock();
            if (stock < qty) {
                return ResponseEntity.badRequest().body("Not enough stock for product: " + p.getName());
            }

            BigDecimal originalPrice = nvl(p.getPrice());
            BigDecimal discountAmount = nvl(p.getDiscount());

            if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
                discountAmount = BigDecimal.ZERO;
            }
            if (discountAmount.compareTo(originalPrice) > 0) {
                discountAmount = originalPrice;
            }

            BigDecimal finalUnitPrice = originalPrice.subtract(discountAmount);
            if (finalUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalUnitPrice = BigDecimal.ZERO;
            }

            BigDecimal originalLineTotal = originalPrice.multiply(BigDecimal.valueOf(qty));
            BigDecimal lineDiscount = discountAmount.multiply(BigDecimal.valueOf(qty));
            BigDecimal finalLineTotal = finalUnitPrice.multiply(BigDecimal.valueOf(qty));

            originalSubtotal = originalSubtotal.add(originalLineTotal);
            productDiscount = productDiscount.add(lineDiscount);

            OrderItem oi = new OrderItem();
            oi.setProduct(p);
            oi.setQty(qty);

            // តម្លៃក្រោយបញ្ចុះ ក្នុង 1 unit
            oi.setUnitPrice(finalUnitPrice.setScale(2, RoundingMode.HALF_UP));

            // line total ក្រោយបញ្ចុះ
            oi.setLineTotal(finalLineTotal.setScale(2, RoundingMode.HALF_UP));

            // optional fields បើ entity របស់អ្នកបានបន្ថែមរួច
            oi.setOriginalPrice(originalPrice.setScale(2, RoundingMode.HALF_UP));
            oi.setDiscountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP));

            items.add(oi);
        }

        if (items.isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty");
        }

        BigDecimal extraDiscount = BigDecimal.valueOf(req.getDiscount());
        if (extraDiscount.compareTo(BigDecimal.ZERO) < 0) {
            extraDiscount = BigDecimal.ZERO;
        }

        BigDecimal maxExtraDiscount = originalSubtotal.subtract(productDiscount);
        if (maxExtraDiscount.compareTo(BigDecimal.ZERO) < 0) {
            maxExtraDiscount = BigDecimal.ZERO;
        }
        if (extraDiscount.compareTo(maxExtraDiscount) > 0) {
            extraDiscount = maxExtraDiscount;
        }

        BigDecimal totalDiscount = productDiscount.add(extraDiscount);
        BigDecimal total = originalSubtotal.subtract(totalDiscount);

        originalSubtotal = originalSubtotal.setScale(2, RoundingMode.HALF_UP);
        productDiscount = productDiscount.setScale(2, RoundingMode.HALF_UP);
        extraDiscount = extraDiscount.setScale(2, RoundingMode.HALF_UP);
        totalDiscount = totalDiscount.setScale(2, RoundingMode.HALF_UP);
        total = total.setScale(2, RoundingMode.HALF_UP);

        Order order = new Order();
        order.setInvoice(invoiceService.nextInvoice());
        order.setCustomerName(customerName);
        order.setPhone(req.getPhone());
        order.setAddress(req.getAddress());

        // ✅ important
        order.setSubtotal(originalSubtotal); // តម្លៃដើម
        order.setDiscount(totalDiscount);    // product discount + extra discount
        order.setTotal(total);               // grand total
        order.setPaymentMethod(pm);

        for (OrderItem oi : items) {
            oi.setOrder(order);
        }
        order.setItems(items);

        int totalItems = items.stream()
                .mapToInt(OrderItem::getQty)
                .sum();
        order.setTotalItems(totalItems);

        if (pm == PaymentMethod.CASH) {
            for (OrderItem oi : items) {
                Product p = oi.getProduct();
                int qty = oi.getQty();

                int stock = p.getStock() == null ? 0 : p.getStock();
                if (stock < qty) {
                    return ResponseEntity.badRequest().body("Not enough stock for product: " + p.getName());
                }

                p.setStock(stock - qty);
                productService.save(p);
            }

            order.setStatus(OrderStatus.PAID);
            order = orderService.save(order);
            return ResponseEntity.ok(new PosCheckoutResponse(order.getId(), "/admin/orders"));
        }

        order.setStatus(OrderStatus.PENDING);
        order = orderService.save(order);

        BakongQrService.KhqrResult qr = bakongQrService.generate(order.getTotal(), order.getInvoice());

        order.setKhqrString(qr.khqrString());
        order.setMd5(qr.md5());
        order = orderService.save(order);

        return ResponseEntity.ok(
                new PosCheckoutResponse(
                        order.getId(),
                        order.getInvoice(),
                        order.getTotal(),
                        order.getMd5(),
                        order.getKhqrString()
                )
        );
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}