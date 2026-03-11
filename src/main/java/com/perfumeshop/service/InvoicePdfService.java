package com.perfumeshop.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
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

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.format.DateTimeFormatter;

@Service
public class InvoicePdfService {

    private static final Color GOLD = new Color(191, 155, 96);
    private static final Color DARK = new Color(35, 35, 35);
    private static final Color MUTED = new Color(120, 120, 120);
    private static final Color LIGHT = new Color(245, 242, 236);
    private static final Color BORDER = new Color(222, 214, 198);
    private static final Color RED = new Color(190, 50, 50);

    public byte[] generate(Order order) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 42, 42, 42, 42);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font brandFont = new Font(Font.TIMES_ROMAN, 30, Font.BOLD, GOLD);
            Font invoiceTitleFont = new Font(Font.HELVETICA, 24, Font.BOLD, GOLD);
            Font sectionFont = new Font(Font.HELVETICA, 11, Font.BOLD, GOLD);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, DARK);
            Font mutedFont = new Font(Font.HELVETICA, 9, Font.NORMAL, MUTED);
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, DARK);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font totalFont = new Font(Font.HELVETICA, 12, Font.BOLD, DARK);
            Font redFont = new Font(Font.HELVETICA, 10, Font.BOLD, RED);

            // ===== TOP HEADER =====
            PdfPTable top = new PdfPTable(2);
            top.setWidthPercentage(100);
            top.setWidths(new float[]{58f, 42f});
            top.setSpacingAfter(24f);

            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);
            left.setPadding(0f);

            PdfPTable brandWrap = new PdfPTable(2);
            brandWrap.setWidthPercentage(100);
            brandWrap.setWidths(new float[]{12f, 88f});

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setPadding(0f);
            logoCell.setPaddingTop(2f);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);


            PdfPCell brandCell = new PdfPCell();
            brandCell.setBorder(Rectangle.NO_BORDER);
            brandCell.setPaddingLeft(6f);
            brandCell.setPaddingTop(0f);
            brandCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph shop = new Paragraph("Fragrance Haven", brandFont);
            shop.setSpacingAfter(6f);
            brandCell.addElement(shop);

            brandCell.addElement(new Paragraph("Premium Perfume Store", mutedFont));
            brandCell.addElement(new Paragraph("Phnom Penh, Cambodia", mutedFont));
            brandCell.addElement(new Paragraph("Phone: +855 xx xxx xxx", mutedFont));
            brandCell.addElement(new Paragraph("Email: support@fragrancehaven.com", mutedFont));

            brandWrap.addCell(logoCell);
            brandWrap.addCell(brandCell);

            left.addElement(brandWrap);

            PdfPCell right = new PdfPCell();
            right.setBorder(Rectangle.NO_BORDER);
            right.setPadding(0f);
            right.setVerticalAlignment(Element.ALIGN_TOP);

            Paragraph invoice = new Paragraph("INVOICE", invoiceTitleFont);
            invoice.setAlignment(Element.ALIGN_RIGHT);
            invoice.setSpacingAfter(14f);
            right.addElement(invoice);

            right.addElement(metaRow("Invoice #", safe(order.getInvoice()), sectionFont, boldFont));
            right.addElement(metaRow("Date", formatDate(order), sectionFont, normalFont));
            right.addElement(metaRow("Payment", prettyEnum(order.getPaymentMethod()), sectionFont, normalFont));
            right.addElement(metaRow("Status", prettyEnum(order.getStatus()), sectionFont, normalFont));

            top.addCell(left);
            top.addCell(right);
            document.add(top);

            // ===== BILLED TO =====
            Paragraph billToTitle = new Paragraph("Billed To", sectionFont);
            billToTitle.setSpacingAfter(8f);
            document.add(billToTitle);

            PdfPTable billWrap = new PdfPTable(1);
            billWrap.setWidthPercentage(100);
            billWrap.setSpacingAfter(18f);

            PdfPCell billCell = new PdfPCell();
            billCell.setPadding(14f);
            billCell.setBorderColor(BORDER);
            billCell.setBackgroundColor(LIGHT);

            Paragraph customerName = new Paragraph(safe(order.getCustomerName()), boldFont);
            customerName.setSpacingAfter(5f);
            billCell.addElement(customerName);

            billCell.addElement(new Paragraph("Phone: " + safe(order.getPhone()), normalFont));
            billCell.addElement(new Paragraph("Address: " + safe(order.getAddress()), normalFont));

            billWrap.addCell(billCell);
            document.add(billWrap);

            // ===== ITEMS =====
            Paragraph itemsTitle = new Paragraph("Items", sectionFont);
            itemsTitle.setSpacingAfter(8f);
            document.add(itemsTitle);

            PdfPTable itemTable = new PdfPTable(6);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{34f, 9f, 13f, 13f, 13f, 18f});
            itemTable.setSpacingAfter(18f);

            addHeader(itemTable, "Description", headerFont);
            addHeader(itemTable, "Qty", headerFont);
            addHeader(itemTable, "Original", headerFont);
            addHeader(itemTable, "Unit", headerFont);
            addHeader(itemTable, "Disc.", headerFont);
            addHeader(itemTable, "Amount", headerFont);

            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (OrderItem it : order.getItems()) {
                    String productName = it.getProduct() != null ? safe(it.getProduct().getName()) : "-";
                    String size = (it.getProduct() != null &&
                            it.getProduct().getSize() != null &&
                            !it.getProduct().getSize().isBlank())
                            ? " / " + it.getProduct().getSize()
                            : "";

                    BigDecimal originalPrice = nvl(it.getOriginalPrice());
                    BigDecimal unitPrice = nvl(it.getUnitPrice());
                    BigDecimal discountAmount = nvl(it.getDiscountAmount());
                    BigDecimal lineTotal = nvl(it.getLineTotal());

                    itemTable.addCell(bodyCell(productName + size, normalFont, Element.ALIGN_LEFT));
                    itemTable.addCell(bodyCell(String.valueOf(it.getQty()), normalFont, Element.ALIGN_CENTER));
                    itemTable.addCell(bodyCell(money(originalPrice), normalFont, Element.ALIGN_RIGHT));
                    itemTable.addCell(bodyCell(money(unitPrice), normalFont, Element.ALIGN_RIGHT));
                    itemTable.addCell(bodyCell(money(discountAmount), redFont, Element.ALIGN_RIGHT));
                    itemTable.addCell(bodyCell(money(lineTotal), boldFont, Element.ALIGN_RIGHT));
                }
            } else {
                PdfPCell empty = new PdfPCell(new Phrase("No items", normalFont));
                empty.setColspan(6);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                empty.setPadding(12f);
                empty.setBorderColor(BORDER);
                itemTable.addCell(empty);
            }

            document.add(itemTable);

            // ===== SUMMARY =====
            PdfPTable wrap = new PdfPTable(2);
            wrap.setWidthPercentage(100);
            wrap.setWidths(new float[]{56f, 44f});
            wrap.setSpacingAfter(30f);

            PdfPCell blank = new PdfPCell(new Phrase(""));
            blank.setBorder(Rectangle.NO_BORDER);

            PdfPCell summaryHolder = new PdfPCell();
            summaryHolder.setBorder(Rectangle.NO_BORDER);
            summaryHolder.setPadding(0f);

            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(100);
            summary.setWidths(new float[]{55f, 45f});

            addSummaryRow(summary, "Subtotal", money(order.getSubtotal()), normalFont, boldFont, false);

            if (nvl(order.getDiscount()).compareTo(BigDecimal.ZERO) > 0) {
                addSummaryRow(summary, "Discount", "-" + money(order.getDiscount()), normalFont, redFont, false);
            }

            addSummaryRow(summary, "Grand Total", money(order.getTotal()), totalFont, totalFont, true);

            summaryHolder.addElement(summary);

            wrap.addCell(blank);
            wrap.addCell(summaryHolder);

            document.add(wrap);

            // ===== FOOTER =====
            Paragraph noteTitle = new Paragraph("Notes", sectionFont);
            noteTitle.setSpacingAfter(6f);
            document.add(noteTitle);

            Paragraph thanks = new Paragraph("Thank you for shopping with Fragrance Haven.", normalFont);
            thanks.setSpacingAfter(4f);
            document.add(thanks);

            Paragraph footer = new Paragraph("Luxury fragrance, elegant experience.", mutedFont);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Cannot generate invoice PDF", e);
        }
    }

    private Paragraph metaRow(String label, String value, Font labelFont, Font valueFont) {
        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_RIGHT);
        p.setSpacingAfter(5f);
        p.add(new Phrase(label + ": ", labelFont));
        p.add(new Phrase(value, valueFont));
        return p;
    }

    private void addHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(GOLD);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(9f);
        cell.setBorderColor(GOLD);
        table.addCell(cell);
    }

    private PdfPCell bodyCell(String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(9f);
        c.setBorderColor(BORDER);
        return c;
    }

    private void addSummaryRow(PdfPTable table,
                               String label,
                               String value,
                               Font leftFont,
                               Font rightFont,
                               boolean highlight) {

        PdfPCell left = new PdfPCell(new Phrase(label, leftFont));
        left.setPadding(10f);
        left.setHorizontalAlignment(Element.ALIGN_LEFT);
        left.setBorder(Rectangle.BOTTOM);
        left.setBorderColor(BORDER);
        if (highlight) {
            left.setBackgroundColor(LIGHT);
        }

        PdfPCell right = new PdfPCell(new Phrase(value, rightFont));
        right.setPadding(10f);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setBorder(Rectangle.BOTTOM);
        right.setBorderColor(BORDER);
        if (highlight) {
            right.setBackgroundColor(LIGHT);
        }

        table.addCell(left);
        table.addCell(right);
    }

    private Image tryLoadLogo() {
        try {
            URL url = getClass().getResource("/static/images/logo.png");
            if (url == null) {
                url = getClass().getResource("/images/logo.png");
            }
            if (url == null) {
                return null;
            }
            return Image.getInstance(url);
        } catch (Exception e) {
            return null;
        }
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

    private String prettyEnum(Enum<?> e) {
        if (e == null) return "-";
        String raw = e.name().toLowerCase().replace("_", " ");
        String[] parts = raw.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isBlank()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)))
                    .append(p.substring(1))
                    .append(" ");
        }
        return sb.toString().trim();
    }

    private String money(BigDecimal v) {
        BigDecimal value = nvl(v);
        return "$" + value.setScale(2, RoundingMode.HALF_UP);
    }
}