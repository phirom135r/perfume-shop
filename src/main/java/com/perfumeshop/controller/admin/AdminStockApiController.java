package com.perfumeshop.controller.admin;

import com.perfumeshop.dto.DataTableResponse;
import com.perfumeshop.dto.StockMovementRowDto;
import com.perfumeshop.entity.Product;
import com.perfumeshop.entity.StockMovement;
import com.perfumeshop.enums.StockAction; // ✅ FIXED
import com.perfumeshop.service.InventoryService;
import com.perfumeshop.service.ProductService;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/admin/api/stock")
public class AdminStockApiController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    public AdminStockApiController(InventoryService inventoryService, ProductService productService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<Map<String, Object>> products() {
        Page<Product> page = productService.search(
                "", null, true,
                PageRequest.of(0, 1000, Sort.by("name").ascending())
        );

        List<Map<String, Object>> out = new ArrayList<>();
        page.getContent().forEach(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("stock", p.getStock() == null ? 0 : p.getStock());
            out.add(m);
        });
        return out;
    }

    @GetMapping("/dt")
    public DataTableResponse<StockMovementRowDto> datatable(
            @RequestParam(defaultValue = "0") int draw,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int length,
            @RequestParam(name = "search[value]", defaultValue = "") String search,
            @RequestParam(name = "order[0][dir]", defaultValue = "desc") String orderDir
    ) {
        int page = Math.max(0, start / Math.max(1, length));

        Sort sort = "asc".equalsIgnoreCase(orderDir)
                ? Sort.by("id").ascending()
                : Sort.by("id").descending();

        Pageable pageable = PageRequest.of(page, length, sort);

        Page<StockMovement> result = inventoryService.search(search, pageable);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        List<StockMovementRowDto> rows = new ArrayList<>();

        for (StockMovement sm : result.getContent()) {
            StockMovementRowDto dto = new StockMovementRowDto();
            dto.setId(sm.getId());
            dto.setProductId(sm.getProduct().getId());
            dto.setProductName(sm.getProduct().getName());
            dto.setAction(sm.getAction().name());
            dto.setQty(sm.getQty());
            dto.setBeforeStock(sm.getBeforeStock());
            dto.setAfterStock(sm.getAfterStock());
            dto.setNote(sm.getNote());
            dto.setCreatedAt(sm.getCreatedAt() != null ? sm.getCreatedAt().format(fmt) : "");
            rows.add(dto);
        }

        long total = result.getTotalElements();
        long filtered = result.getTotalElements();

        return new DataTableResponse<>(draw, total, filtered, rows);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Long productId = body.get("productId") == null ? null : Long.valueOf(String.valueOf(body.get("productId")));
            StockAction action = body.get("action") == null ? null : StockAction.valueOf(String.valueOf(body.get("action")));
            Integer qty = body.get("qty") == null ? null : Integer.valueOf(String.valueOf(body.get("qty")));
            String note = body.get("note") == null ? "" : String.valueOf(body.get("note"));

            return ResponseEntity.ok(inventoryService.createMovement(productId, action, qty, note));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}