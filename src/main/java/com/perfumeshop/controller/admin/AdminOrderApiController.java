//AdminOrderApiController
package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Order;
import com.perfumeshop.entity.OrderItem;
import com.perfumeshop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/admin/api/orders")
public class AdminOrderApiController {

    private final OrderService orderService;

    public AdminOrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Order details for modal (Order Details page)
     * GET /admin/api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {

        // IMPORTANT: must load items + product (use EntityGraph in repository)
        Order o = orderService.findWithItemsOrThrow(id);

        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem it : o.getItems()) {

            BigDecimal price = (it.getUnitPrice() != null)
                    ? it.getUnitPrice()
                    : (it.getProduct() != null && it.getProduct().getPrice() != null
                    ? it.getProduct().getPrice()
                    : BigDecimal.ZERO);

            BigDecimal amount = (it.getLineTotal() != null)
                    ? it.getLineTotal()
                    : price.multiply(BigDecimal.valueOf(it.getQty()));

            String productName = (it.getProduct() != null && it.getProduct().getName() != null)
                    ? it.getProduct().getName()
                    : "-";

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("product", productName);
            row.put("qty", it.getQty());
            row.put("price", price);
            row.put("amount", amount);
            items.add(row);
        }

        String createdAt = null;
        if (o.getCreatedAt() != null) {
            createdAt = o.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", o.getId());
        data.put("invoice", o.getInvoice());
        data.put("customerName", o.getCustomerName());
        data.put("phone", o.getPhone());
        data.put("address", o.getAddress());
        data.put("paymentMethod", o.getPaymentMethod() != null ? o.getPaymentMethod().name() : "");
        data.put("status", o.getStatus() != null ? o.getStatus().name() : "");
        data.put("createdAt", createdAt); // ✅ send as String for JS

        data.put("subtotal", o.getSubtotal());
        data.put("discount", o.getDiscount());
        data.put("total", o.getTotal());
        data.put("items", items);

        return ResponseEntity.ok(data);
    }
}