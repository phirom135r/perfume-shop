package com.perfumeshop.controller.shop;

import com.perfumeshop.entity.Order;
import com.perfumeshop.entity.OrderItem;
import com.perfumeshop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/perfume-shop/api/my-orders")
public class ShopMyOrderApiController {

    private final OrderService orderService;

    public ShopMyOrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        Order o = orderService.findMyOrderDetailOrThrow(id, email);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", o.getId());
        data.put("invoice", o.getInvoice());
        data.put("customerName", o.getCustomerName());
        data.put("phone", o.getPhone());
        data.put("address", o.getAddress());
        data.put("status", o.getStatus() != null ? o.getStatus().name() : "");
        data.put("paymentMethod", o.getPaymentMethod() != null ? o.getPaymentMethod().name() : "");
        data.put("subtotal", o.getSubtotal());
        data.put("discount", o.getDiscount());
        data.put("total", o.getTotal());
        data.put("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().toString() : "");

        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem it : o.getItems()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("productName", it.getProduct() != null ? it.getProduct().getName() : "-");
            row.put("size", (it.getProduct() != null && it.getProduct().getSize() != null) ? it.getProduct().getSize() : "N/A");
            row.put("image", (it.getProduct() != null) ? it.getProduct().getImage() : null);
            row.put("qty", it.getQty());
            row.put("unitPrice", it.getUnitPrice());
            row.put("lineTotal", it.getLineTotal());
            items.add(row);
        }

        data.put("items", items);

        return ResponseEntity.ok(data);
    }
}