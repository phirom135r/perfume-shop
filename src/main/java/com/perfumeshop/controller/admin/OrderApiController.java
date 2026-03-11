package com.perfumeshop.controller.admin;

import com.perfumeshop.dto.DataTableResponse;
import com.perfumeshop.dto.OrderRowDto;
import com.perfumeshop.entity.Order;
import com.perfumeshop.entity.OrderItem;
import com.perfumeshop.enums.OrderStatus;
import com.perfumeshop.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/admin/api/orders")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/dt")
    public DataTableResponse<OrderRowDto> datatable(
            @RequestParam(defaultValue = "0") int draw,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int length,
            @RequestParam(name = "search[value]", defaultValue = "") String search,
            @RequestParam(name = "order[0][column]", defaultValue = "7") int orderCol,
            @RequestParam(name = "order[0][dir]", defaultValue = "desc") String orderDir
    ) {
        int page = Math.max(0, start / Math.max(1, length));

        Sort sort = buildSort(orderCol, orderDir);
        Pageable pageable = PageRequest.of(page, length, sort);

        Page<OrderRowDto> result = orderService.adminSearchRows(search, null, pageable);

        long total = result.getTotalElements();
        long filtered = result.getTotalElements();

        return new DataTableResponse<>(draw, total, filtered, result.getContent());
    }

    private Sort buildSort(int col, String dir) {
        boolean asc = !"desc".equalsIgnoreCase(dir);

        String prop;
        switch (col) {
            case 0 -> prop = "invoice";
            case 1 -> prop = "customerName";
            case 2 -> prop = "phone";
            case 3 -> prop = "totalItems";
            case 4 -> prop = "total";
            case 5 -> prop = "paymentMethod";
            case 6 -> prop = "status";
            case 7 -> prop = "createdAt";
            default -> prop = "createdAt";
        }

        return asc ? Sort.by(prop).ascending() : Sort.by(prop).descending();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {

        Order o = orderService.findWithItemsOrThrow(id);

        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem it : o.getItems()) {

            BigDecimal originalPrice = it.getOriginalPrice() != null
                    ? it.getOriginalPrice()
                    : (it.getProduct() != null && it.getProduct().getPrice() != null
                    ? it.getProduct().getPrice()
                    : BigDecimal.ZERO);

            BigDecimal unitPrice = it.getUnitPrice() != null
                    ? it.getUnitPrice()
                    : originalPrice;

            BigDecimal discountAmount = it.getDiscountAmount() != null
                    ? it.getDiscountAmount()
                    : originalPrice.subtract(unitPrice).max(BigDecimal.ZERO);

            BigDecimal lineTotal = it.getLineTotal() != null
                    ? it.getLineTotal()
                    : unitPrice.multiply(BigDecimal.valueOf(it.getQty()));

            String productName = (it.getProduct() != null && it.getProduct().getName() != null)
                    ? it.getProduct().getName()
                    : "-";

            String size = (it.getProduct() != null && it.getProduct().getSize() != null)
                    ? it.getProduct().getSize()
                    : "N/A";

            String image = (it.getProduct() != null && it.getProduct().getImage() != null && !it.getProduct().getImage().isBlank())
                    ? it.getProduct().getImage()
                    : "/images/no-image.png";

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("product", productName);
            row.put("size", size);
            row.put("image", image);
            row.put("qty", it.getQty());
            row.put("originalPrice", originalPrice);
            row.put("price", unitPrice);
            row.put("discountAmount", discountAmount);
            row.put("amount", lineTotal);
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
        data.put("createdAt", createdAt);
        data.put("subtotal", o.getSubtotal());
        data.put("discount", o.getDiscount());
        data.put("total", o.getTotal());
        data.put("items", items);

        return ResponseEntity.ok(data);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody Map<String, String> body) {

        String st = body == null ? "" : body.getOrDefault("status", "");
        st = st == null ? "" : st.trim().toUpperCase();

        if (st.isBlank()) {
            return ResponseEntity.badRequest().body("status is required");
        }

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(st);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid status: " + st);
        }

        Order o = orderService.findByIdOrThrow(id);
        o.setStatus(newStatus);
        orderService.save(o);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "status", newStatus.name()
        ));
    }
}