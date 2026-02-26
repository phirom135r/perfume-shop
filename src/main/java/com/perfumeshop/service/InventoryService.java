package com.perfumeshop.service;

import com.perfumeshop.entity.*;
import com.perfumeshop.enums.StockAction;
import com.perfumeshop.repository.StockMovementRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
        import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final ProductService productService;
    private final StockMovementRepository stockRepo;

    public InventoryService(ProductService productService, StockMovementRepository stockRepo) {
        this.productService = productService;
        this.stockRepo = stockRepo;
    }

    public Page<StockMovement> search(String kw, Pageable pageable) {
        return stockRepo.search(kw, pageable);
    }

    @Transactional
    public StockMovement createMovement(Long productId, StockAction action, Integer qtyOrNewStock, String note) {

        if (productId == null) throw new RuntimeException("Product is required");
        if (action == null) throw new RuntimeException("Action is required");

        Product p = productService.findOrThrow(productId);

        int before = (p.getStock() == null) ? 0 : p.getStock();
        int after;
        int qty; // movement qty

        if (action == StockAction.IN) {
            if (qtyOrNewStock == null || qtyOrNewStock <= 0) throw new RuntimeException("Qty must be > 0");
            qty = qtyOrNewStock;
            after = before + qty;

        } else if (action == StockAction.OUT) {
            if (qtyOrNewStock == null || qtyOrNewStock <= 0) throw new RuntimeException("Qty must be > 0");
            qty = qtyOrNewStock;
            after = before - qty;
            if (after < 0) throw new RuntimeException("Stock not enough (would go negative)");

        } else { // ADJUST = set new stock
            if (qtyOrNewStock == null || qtyOrNewStock < 0) throw new RuntimeException("New stock must be >= 0");
            after = qtyOrNewStock;
            qty = after - before; // can be negative
        }

        // update product stock
        p.setStock(after);
        productService.save(p);

        // log movement
        StockMovement sm = new StockMovement();
        sm.setProduct(p);
        sm.setAction(action);
        sm.setBeforeStock(before);
        sm.setAfterStock(after);
        sm.setQty(qty);
        sm.setNote(note == null ? "" : note.trim());

        return stockRepo.save(sm);
    }
}