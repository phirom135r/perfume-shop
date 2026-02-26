package com.perfumeshop.service;

import com.perfumeshop.entity.Order;
import com.perfumeshop.enums.OrderStatus;
import com.perfumeshop.repository.OrderItemRepository;
import com.perfumeshop.repository.OrderRepository;
import com.perfumeshop.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final ProductRepository productRepo; // ✅ ADD

    public DashboardService(OrderRepository orderRepo,
                            OrderItemRepository orderItemRepo,
                            ProductRepository productRepo) { // ✅ ADD
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.productRepo = productRepo;
    }

    // ================= RANGE HELPER =================
    private record Range(LocalDateTime start, LocalDateTime end, String label) {}

    private Range resolveRange(String range, String from, String to) {
        String r = (range == null) ? "LAST_7_DAYS" : range.trim().toUpperCase();
        LocalDate today = LocalDate.now();

        return switch (r) {
            case "TODAY" -> {
                LocalDateTime s = today.atStartOfDay();
                LocalDateTime e = today.plusDays(1).atStartOfDay();
                yield new Range(s, e, "Today");
            }
            case "THIS_MONTH" -> {
                LocalDate first = today.withDayOfMonth(1);
                LocalDateTime s = first.atStartOfDay();
                LocalDateTime e = first.plusMonths(1).atStartOfDay();
                yield new Range(s, e, "This Month");
            }
            case "CUSTOM" -> {
                LocalDate f;
                LocalDate t;
                try { f = (from == null || from.isBlank()) ? today.minusDays(6) : LocalDate.parse(from.trim()); }
                catch (Exception e) { f = today.minusDays(6); }

                try { t = (to == null || to.isBlank()) ? today : LocalDate.parse(to.trim()); }
                catch (Exception e) { t = today; }

                LocalDateTime s = f.atStartOfDay();
                LocalDateTime e = t.plusDays(1).atStartOfDay(); // include 'to' day
                yield new Range(s, e, "Custom");
            }
            default -> {
                LocalDate start = today.minusDays(6);
                LocalDateTime s = start.atStartOfDay();
                LocalDateTime e = today.plusDays(1).atStartOfDay();
                yield new Range(s, e, "Last 7 days");
            }
        };
    }

    private String buildLabel(Range rg) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate from = rg.start.toLocalDate();
        LocalDate toInclusive = rg.end.minusDays(1).toLocalDate();
        return f.format(from) + " - " + f.format(toInclusive);
    }

    // ================= SUMMARY =================
    public Map<String, Object> summary(String range, String from, String to) {

        Range rg = resolveRange(range, from, to);

        BigDecimal sales = orderRepo.sumTotalBetween(OrderStatus.PAID, rg.start, rg.end);
        long orders = orderRepo.countPaidBetween(OrderStatus.PAID, rg.start, rg.end);

        long pending = orderRepo.countByStatusBetween(OrderStatus.PENDING, rg.start, rg.end);
        long paid = orderRepo.countByStatusBetween(OrderStatus.PAID, rg.start, rg.end);
        long cancelled = orderRepo.countByStatusBetween(OrderStatus.CANCELLED, rg.start, rg.end);

        List<Order> recent = orderRepo.findRecentBetween(rg.start, rg.end, PageRequest.of(0, 8));

        // ✅ Inventory KPIs (from ProductRepository)
        int threshold = 5; // you can change later or make it config
        long totalProducts = productRepo.countAllProducts();
        long totalStockUnits = productRepo.sumAllStock();
        long lowStockCount = productRepo.countLowStock(threshold);
        long outOfStockCount = productRepo.countOutOfStock();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("rangeLabel", buildLabel(rg));

        res.put("sales", sales);
        res.put("orders", orders);

        res.put("pendingCount", pending);
        res.put("paidCount", paid);
        res.put("cancelledCount", cancelled);

        // ✅ send to JS (IDs in your dashboard.js)
        res.put("totalProducts", totalProducts);
        res.put("totalStockUnits", totalStockUnits);
        res.put("lowStockThreshold", threshold);
        res.put("lowStockCount", lowStockCount);
        res.put("outOfStockCount", outOfStockCount);

        List<Map<String, Object>> recentDto = new ArrayList<>();
        for (Order o : recent) {
            recentDto.add(Map.of(
                    "id", o.getId(),
                    "invoice", o.getInvoice(),
                    "customerName", o.getCustomerName(),
                    "total", o.getTotal(),
                    "status", o.getStatus() != null ? o.getStatus().name() : "",
                    "createdAt", o.getCreatedAt()
            ));
        }
        res.put("recentOrders", recentDto);

        return res;
    }

    // sales(...) and topProducts(...) keep your old code
    public List<Map<String, Object>> sales(String range, String from, String to) {
        Range rg = resolveRange(range, from, to);

        List<Object[]> rows = orderRepo.dailyRevenue(OrderStatus.PAID, rg.start, rg.end);

        LocalDate start = rg.start.toLocalDate();
        LocalDate endExclusive = rg.end.toLocalDate();

        Map<LocalDate, BigDecimal> map = new HashMap<>();
        for (Object[] r : rows) {
            LocalDate d;
            Object dateObj = r[0];
            if (dateObj instanceof java.sql.Date sd) d = sd.toLocalDate();
            else d = LocalDate.parse(String.valueOf(dateObj));

            BigDecimal total = (r[1] instanceof BigDecimal bd) ? bd : new BigDecimal(String.valueOf(r[1]));
            map.put(d, total);
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (LocalDate d = start; d.isBefore(endExclusive); d = d.plusDays(1)) {
            out.add(Map.of(
                    "date", d.toString(),
                    "total", map.getOrDefault(d, BigDecimal.ZERO)
            ));
        }
        return out;
    }

    public List<Map<String, Object>> topProducts(String range, String from, String to) {
        Range rg = resolveRange(range, from, to);

        List<Object[]> rows = orderItemRepo.topProducts(OrderStatus.PAID, rg.start, rg.end);

        List<Map<String, Object>> out = new ArrayList<>();
        int limit = Math.min(rows.size(), 8);
        for (int i = 0; i < limit; i++) {
            Object[] r = rows.get(i);
            out.add(Map.of(
                    "name", String.valueOf(r[0]),
                    "qty", ((Number) r[1]).longValue(),
                    "total", r[2]
            ));
        }
        return out;
    }
}