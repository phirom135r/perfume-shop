package com.perfumeshop.service;


import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.perfumeshop.entity.Order;
import com.perfumeshop.entity.OrderItem;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class InvoicePdfService {

    public byte[] generate(Order order) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(34, 34, 34));
            Font subTitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(110, 110, 110));
            Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(40, 40, 40));
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(35, 35, 35));
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(20, 20, 20));
            Font smallMutedFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(120, 120, 120));
            Font redFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(200, 40, 40));
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);

            // ===== Header =====
            Paragraph shopName = new Paragraph("Fragrance Haven", titleFont);
            shopName.setAlignment(Element.ALIGN_LEFT);
            shopName.setSpacingAfter(6f);
            document.add(shopName);

            Paragraph invoiceTitle = new Paragraph("Invoice", subTitleFont);
            invoiceTitle.setSpacingAfter(18f);
            document.add(invoiceTitle);

            // ===== Top Info =====
            PdfPTable topTable = new PdfPTable(2);
            topTable.setWidthPercentage(100);
            topTable.setWidths(new float[]{58f, 42f});
            topTable.setSpacingAfter(22f);

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setPadding(0f);

            Paragraph billTo = new Paragraph("Bill To", sectionFont);
            billTo.setSpacingAfter(6f);
            leftCell.addElement(billTo);

            Paragraph customerName = new Paragraph(safe(order.getCustomerName()), boldFont);
            customerName.setSpacingAfter(4f);
            leftCell.addElement(customerName);

            Paragraph phone = new Paragraph("Phone: " + safe(order.getPhone()), normalFont);
            phone.setSpacingAfter(4f);
            leftCell.addElement(phone);

            Paragraph address = new Paragraph("Address: " + safe(order.getAddress()), normalFont);
            address.setSpacingAfter(2f);
            leftCell.addElement(address);

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setPadding(0f);

            Paragraph invoiceNo = new Paragraph("Invoice No: " + safe(order.getInvoice()), boldFont);
            invoiceNo.setSpacingAfter(4f);
            rightCell.addElement(invoiceNo);

            Paragraph date = new Paragraph("Date: " + formatDate(order), normalFont);
            date.setSpacingAfter(4f);
            rightCell.addElement(date);

            Paragraph payment = new Paragraph("Payment: " + safeEnum(order.getPaymentMethod()), normalFont);
            payment.setSpacingAfter(4f);
            rightCell.addElement(payment);

            Paragraph status = new Paragraph("Status: " + safeEnum(order.getStatus()), normalFont);
            rightCell.addElement(status);

            topTable.addCell(leftCell);
            topTable.addCell(rightCell);

            document.add(topTable);

            // ===== Items Table =====
            PdfPTable itemTable = new PdfPTable(6);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{32f, 10f, 14f, 14f, 12f, 18f});
            itemTable.setSpacingAfter(25f);

            addHeader(itemTable, "Product", headerFont);
            addHeader(itemTable, "Qty", headerFont);
            addHeader(itemTable, "Original", headerFont);
            addHeader(itemTable, "Unit", headerFont);
            addHeader(itemTable, "Disc.", headerFont);
            addHeader(itemTable, "Amount", headerFont);

            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (OrderItem it : order.getItems()) {
                    String productName = it.getProduct() != null ? safe(it.getProduct().getName()) : "-";
                    String size = (it.getProduct() != null && it.getProduct().getSize() != null && !it.getProduct().getSize().isBlank())
                            ? " / " + it.getProduct().getSize()
                            : "";

                    BigDecimal originalPrice = nvl(it.getOriginalPrice());
                    BigDecimal unitPrice = nvl(it.getUnitPrice());
                    BigDecimal discountAmount = nvl(it.getDiscountAmount());
                    BigDecimal lineTotal = nvl(it.getLineTotal());

                    itemTable.addCell(cell(productName + size, normalFont, Element.ALIGN_LEFT));
                    itemTable.addCell(cell(String.valueOf(it.getQty()), normalFont, Element.ALIGN_CENTER));
                    itemTable.addCell(cell(money(originalPrice), normalFont, Element.ALIGN_RIGHT));
                    itemTable.addCell(cell(money(unitPrice), normalFont, Element.ALIGN_RIGHT));
                    itemTable.addCell(cell(money(discountAmount), redFont, Element.ALIGN_RIGHT));
                    itemTable.addCell(cell(money(lineTotal), boldFont, Element.ALIGN_RIGHT));
                }
            } else {
                PdfPCell empty = new PdfPCell(new Phrase("No items", normalFont));
                empty.setColspan(6);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                empty.setPadding(12f);
                empty.setBorder(Rectangle.BOX);
                itemTable.addCell(empty);
            }

            document.add(itemTable);

            // ===== Summary =====
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setWidthPercentage(38);
            summaryTable.setWidths(new float[]{55f, 45f});
            summaryTable.setSpacingBefore(10f);
            summaryTable.setSpacingAfter(30f);

            addSummaryRow(summaryTable, "Subtotal", money(order.getSubtotal()), normalFont, boldFont);
            addSummaryRow(summaryTable, "Discount", "-" + money(order.getDiscount()), normalFont, redFont);
            addSummaryRow(summaryTable, "Grand Total", money(order.getTotal()), boldFont, boldFont);

            document.add(summaryTable);

            // ===== Footer =====
            Paragraph thanks = new Paragraph("Thank you for shopping with Fragrance Haven.", smallMutedFont);
            thanks.setSpacingBefore(40f);
            thanks.setAlignment(Element.ALIGN_CENTER);
            document.add(thanks);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Cannot generate invoice PDF", e);
        }
    }

    private void addHeader(PdfPTable table, String text, Font headerFont) {
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(new Color(59, 91, 219));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(10f);
        table.addCell(cell);
    }

    private PdfPCell cell(String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(10f);
        return c;
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font leftFont, Font rightFont) {
        PdfPCell left = new PdfPCell(new Phrase(label, leftFont));
        left.setBorder(Rectangle.BOTTOM);
        left.setPadding(8f);
        left.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell right = new PdfPCell(new Phrase(value, rightFont));
        right.setBorder(Rectangle.BOTTOM);
        right.setPadding(8f);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(left);
        table.addCell(right);
    }

    private String formatDate(Order order) {
        if (order.getCreatedAt() == null) return "-";
        return order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String safe(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    private String safeEnum(Enum<?> e) {
        return e == null ? "-" : e.name();
    }

    private String money(BigDecimal v) {
        BigDecimal value = nvl(v);
        return "$" + value.setScale(2, RoundingMode.HALF_UP);
    }
}