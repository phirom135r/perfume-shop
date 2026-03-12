package com.perfumeshop.service;

import com.perfumeshop.entity.Order;
import com.perfumeshop.entity.OrderItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class TelegramMessageBuilder {

    private static final DateTimeFormatter DF =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String buildNewOrderMessage(Order order) {
        StringBuilder sb = new StringBuilder();

        sb.append("✨ <b>FRAGRANCE HAVEN | NEW ORDER</b>\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━\n");
        appendOrderInfo(sb, order);
        appendItems(sb, order);

        return sb.toString();
    }

    public String buildPaidOrderMessage(Order order) {
        StringBuilder sb = new StringBuilder();

        sb.append("✅ <b>FRAGRANCE HAVEN | ORDER PAID</b>\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━\n");
        appendOrderInfo(sb, order);
        appendItems(sb, order);

        return sb.toString();
    }

    public String buildCancelledOrderMessage(Order order) {
        StringBuilder sb = new StringBuilder();

        sb.append("❌ <b>FRAGRANCE HAVEN | ORDER CANCELLED</b>\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━\n");
        appendOrderInfo(sb, order);
        appendItems(sb, order);

        return sb.toString();
    }

    private void appendOrderInfo(StringBuilder sb, Order order) {
        sb.append("🧾 <b>Invoice:</b> ").append(safe(order.getInvoice())).append("\n");
        sb.append("👤 <b>Customer:</b> ").append(safe(order.getCustomerName())).append("\n");
        sb.append("📞 <b>Phone:</b> ").append(safe(order.getPhone())).append("\n");
        sb.append("📍 <b>Address:</b> ").append(safe(order.getAddress())).append("\n");
        sb.append("💳 <b>Payment:</b> ").append(enumText(order.getPaymentMethod())).append("\n");
        sb.append("📌 <b>Status:</b> ").append(enumText(order.getStatus())).append("\n");
        sb.append("💰 <b>Total:</b> ").append(money(order.getTotal())).append("\n");
        sb.append("🕒 <b>Date:</b> ").append(formatDate(order)).append("\n");
    }

    private void appendItems(StringBuilder sb, Order order) {
        sb.append("\n");
        sb.append("🛍️ <b>Items</b>\n");

        if (order.getItems() == null || order.getItems().isEmpty()) {
            sb.append("• No items\n");
            return;
        }

        int index = 1;
        for (OrderItem it : order.getItems()) {
            String name = "-";
            if (it.getProduct() != null && it.getProduct().getName() != null && !it.getProduct().getName().isBlank()) {
                name = it.getProduct().getName();
            }

            String size = "";
            if (it.getProduct() != null && it.getProduct().getSize() != null && !it.getProduct().getSize().isBlank()) {
                size = " • " + it.getProduct().getSize();
            }

            sb.append(index++)
                    .append(". <b>")
                    .append(escapeHtml(name))
                    .append("</b>")
                    .append(size)
                    .append("\n");

            sb.append("   Qty: ")
                    .append(it.getQty())
                    .append(" × ")
                    .append(money(it.getUnitPrice()))
                    .append(" = ")
                    .append(money(it.getLineTotal()))
                    .append("\n");
        }
    }

    private String formatDate(Order order) {
        if (order.getCreatedAt() == null) return "-";
        return order.getCreatedAt().format(DF);
    }

    private String safe(String s) {
        return s == null || s.isBlank() ? "-" : escapeHtml(s);
    }

    private String enumText(Enum<?> e) {
        return e == null ? "-" : escapeHtml(e.name());
    }

    private String money(BigDecimal v) {
        if (v == null) return "$0.00";
        return "$" + v.setScale(2, RoundingMode.HALF_UP);
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}