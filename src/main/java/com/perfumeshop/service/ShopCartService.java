package com.perfumeshop.service;

import com.perfumeshop.dto.CartItemDto;
import com.perfumeshop.entity.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ShopCartService {

    private static final String CART_KEY = "SHOP_CART";

    private final ProductService productService;

    public ShopCartService(ProductService productService) {
        this.productService = productService;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, CartItemDto> getCartMap(HttpSession session) {
        Object obj = session.getAttribute(CART_KEY);
        if (obj instanceof Map<?, ?> map) {
            return (Map<Long, CartItemDto>) map;
        }
        Map<Long, CartItemDto> cart = new LinkedHashMap<>();
        session.setAttribute(CART_KEY, cart);
        return cart;
    }

    public Collection<CartItemDto> getItems(HttpSession session) {
        return getCartMap(session).values();
    }

    public int getCartCount(HttpSession session) {
        return getCartMap(session).values().stream()
                .mapToInt(i -> i.getQty() == null ? 0 : i.getQty())
                .sum();
    }

    // total តម្លៃបន្ទាប់ពីបញ្ចុះ
    public BigDecimal getSubtotal(HttpSession session) {
        return getCartMap(session).values().stream()
                .map(CartItemDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // total តម្លៃដើម
    public BigDecimal getOriginalSubtotal(HttpSession session) {
        return getCartMap(session).values().stream()
                .map(CartItemDto::getOriginalLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // total discount
    public BigDecimal getDiscount(HttpSession session) {
        return getCartMap(session).values().stream()
                .map(CartItemDto::getLineDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addToCart(Long productId, int qty, HttpSession session) {
        if (productId == null || qty <= 0) {
            throw new RuntimeException("Invalid product or qty");
        }

        Product p = productService.findOrThrow(productId);

        if (!Boolean.TRUE.equals(p.getActive())) {
            throw new RuntimeException("Product is inactive");
        }

        int stock = p.getStock() == null ? 0 : p.getStock();
        if (stock <= 0) {
            throw new RuntimeException("Product is out of stock");
        }

        BigDecimal originalPrice = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
        BigDecimal discount = p.getDiscount() == null ? BigDecimal.ZERO : p.getDiscount();

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = BigDecimal.ZERO;
        }
        if (discount.compareTo(originalPrice) > 0) {
            discount = originalPrice;
        }

        BigDecimal finalPrice = originalPrice.subtract(discount);

        Map<Long, CartItemDto> cart = getCartMap(session);
        CartItemDto existing = cart.get(productId);

        if (existing == null) {
            int safeQty = Math.min(qty, stock);

            CartItemDto item = new CartItemDto(
                    p.getId(),
                    p.getName(),
                    p.getImage(),
                    p.getSize(),
                    originalPrice,
                    finalPrice,
                    discount,
                    safeQty,
                    stock
            );
            cart.put(productId, item);
        } else {
            int newQty = existing.getQty() + qty;
            if (newQty > stock) newQty = stock;

            existing.setQty(newQty);
            existing.setStock(stock);
            existing.setOriginalPrice(originalPrice);
            existing.setUnitPrice(finalPrice);
            existing.setDiscountAmount(discount);
            existing.setSize(p.getSize());
            existing.setImage(p.getImage());
            existing.setName(p.getName());
        }
    }

    public void updateQty(Long productId, int qty, HttpSession session) {
        Map<Long, CartItemDto> cart = getCartMap(session);
        CartItemDto item = cart.get(productId);
        if (item == null) return;

        if (qty <= 0) {
            cart.remove(productId);
            return;
        }

        Product p = productService.findOrThrow(productId);
        int stock = p.getStock() == null ? 0 : p.getStock();

        if (qty > stock) qty = stock;

        BigDecimal originalPrice = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
        BigDecimal discount = p.getDiscount() == null ? BigDecimal.ZERO : p.getDiscount();

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = BigDecimal.ZERO;
        }
        if (discount.compareTo(originalPrice) > 0) {
            discount = originalPrice;
        }

        BigDecimal finalPrice = originalPrice.subtract(discount);

        item.setQty(qty);
        item.setStock(stock);
        item.setOriginalPrice(originalPrice);
        item.setUnitPrice(finalPrice);
        item.setDiscountAmount(discount);
        item.setSize(p.getSize());
        item.setImage(p.getImage());
        item.setName(p.getName());
    }

    public void removeItem(Long productId, HttpSession session) {
        getCartMap(session).remove(productId);
    }

    public void clear(HttpSession session) {
        session.removeAttribute(CART_KEY);
    }
}