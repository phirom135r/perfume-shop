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

    private static final Color PRIMARY = new Color(108, 156, 143);   // soft green
    private static final Color DARK = new Color(40, 40, 40);
    private static final Color MUTED = new Color(110, 110, 110);
    private static final Color LIGHT_LINE = new Color(210, 220, 216);
    private static final Color SOFT_BG = new Color(245, 248, 247);
    private static final Color RED = new Color(200, 40, 40);

    public byte[] generate(Order order) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 42, 42, 42, 42);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font shopFont = new Font(Font.HELVETICA, 20, Font.BOLD, DARK);
            Font titleFont = new Font(Font.HELVETICA, 28, Font.BOLD, PRIMARY);
            Font labelFont = new Font(Font.HELVETICA, 11, Font.BOLD, PRIMARY);
            Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL, DARK);
            Font mutedFont = new Font(Font.HELVETICA, 10, Font.NORMAL, MUTED);
            Font boldFont = new Font(Font.HELVETICA, 11, Font.BOLD, DARK);
            Font tableHeaderFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
            Font totalFont = new Font(Font.HELVETICA, 13, Font.BOLD, PRIMARY);
            Font redFont = new Font(Font.HELVETICA, 11, Font.BOLD, RED);

            // ===== TOP HEADER =====
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{55f, 45f});
            headerTable.setSpacingAfter(26f);

            PdfPCell leftHeader = new PdfPCell();
            leftHeader.setBorder(Rectangle.NO_BORDER);
            leftHeader.setPadding(0f);

            // Optional logo
            Image logo = tryLoadLogo();
            if (logo != null) {
                logo.scaleToFit(110, 55);
                logo.setAlignment(Image.LEFT);
                leftHeader.addElement(logo);
            }

            Paragraph shopName = new Paragraph("Fragrance Haven", shopFont);
            shopName.setSpacingBefore(4f);
            shopName.setSpacingAfter(6f);
            leftHeader.addElement(shopName);

            leftHeader.addElement(new Paragraph("Phnom Penh, Cambodia", normalFont));
            leftHeader.addElement(new Paragraph("Phone: +855 xx xxx xxx", normalFont));
            leftHeader.addElement(new Paragraph("Email: support@fragrancehaven.com", normalFont));

            PdfPCell rightHeader = new PdfPCell();
            rightHeader.setBorder(Rectangle.NO_BORDER);
            rightHeader.setPadding(0f);
            rightHeader.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Paragraph receiptTitle = new Paragraph("INVOICE", titleFont);
            receiptTitle.setAlignment(Element.ALIGN_RIGHT);
            receiptTitle.setSpacingAfter(18f);
            rightHeader.addElement(receiptTitle);

            rightHeader.addElement(metaParagraph("Invoice #", safe(order.getInvoice()), labelFont, boldFont));
            rightHeader.addElement(metaParagraph("Date", formatDate(order), labelFont, normalFont));
            rightHeader.addElement(metaParagraph("Payment", safeEnum(order.getPaymentMethod()), labelFont, normalFont));
            rightHeader.addElement(metaParagraph("Status", safeEnum(order.getStatus()), labelFont, normalFont));

            headerTable.addCell(leftHeader);
            headerTable.addCell(rightHeader);
            document.add(headerTable);

            // ===== BILL TO =====
            PdfPTable billTable = new PdfPTable(1);
            billTable.setWidthPercentage(100);
            billTable.setSpacingAfter(22f);

            PdfPCell billCell = new PdfPCell();
            billCell.setBorder(Rectangle.NO_BORDER);
            billCell.setPadding(0f);

            Paragraph billTitle = new Paragraph("Billed To", labelFont);
            billTitle.setSpacingAfter(8f);
            billCell.addElement(billTitle);

            Paragraph customerName = new Paragraph(safe(order.getCustomerName()), boldFont);
            customerName.setSpacingAfter(5f);
            billCell.addElement(customerName);

            billCell.addElement(new Paragraph("Phone: " + safe(order.getPhone()), normalFont));
            billCell.addElement(new Paragraph("Address: " + safe(order.getAddress()), normalFont));

            billTable.addCell(billCell);
            document.add(billTable);

            // ===== ITEMS TITLE =====
            Paragraph itemsTitle = new Paragraph("Items", labelFont);
            itemsTitle.setSpacingAfter(8f);
            document.add(itemsTitle);

            // ===== ITEMS TABLE =====
            PdfPTable itemTable = new PdfPTable(6);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{34f, 9f, 14f, 14f, 11f, 18f});
            itemTable.setSpacingAfter(20f);

            addHeader(itemTable, "Description", tableHeaderFont);
            addHeader(itemTable, "Qty", tableHeaderFont);
            addHeader(itemTable, "Original", tableHeaderFont);
            addHeader(itemTable, "Unit Price", tableHeaderFont);
            addHeader(itemTable, "Discount", tableHeaderFont);
            addHeader(itemTable, "Amount", tableHeaderFont);

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
                empty.setBorderColor(LIGHT_LINE);
                itemTable.addCell(empty);
            }

            document.add(itemTable);

            // ===== SUMMARY =====
            PdfPTable summaryWrap = new PdfPTable(2);
            summaryWrap.setWidthPercentage(100);
            summaryWrap.setWidths(new float[]{55f, 45f});
            summaryWrap.setSpacingAfter(30f);

            PdfPCell blankLeft = new PdfPCell(new Phrase(""));
            blankLeft.setBorder(Rectangle.NO_BORDER);

            PdfPCell summaryRight = new PdfPCell();
            summaryRight.setBorder(Rectangle.NO_BORDER);
            summaryRight.setPadding(0f);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setWidths(new float[]{55f, 45f});

            addSummaryRow(summaryTable, "Subtotal", money(order.getSubtotal()), normalFont, boldFont, false);
            addSummaryRow(summaryTable, "Discount", "-" + money(order.getDiscount()), normalFont, redFont, false);
            addSummaryRow(summaryTable, "Grand Total", money(order.getTotal()), totalFont, totalFont, true);

            summaryRight.addElement(summaryTable);

            summaryWrap.addCell(blankLeft);
            summaryWrap.addCell(summaryRight);

            document.add(summaryWrap);

            // ===== FOOTER =====
            Paragraph noteTitle = new Paragraph("Notes", labelFont);
            noteTitle.setSpacingAfter(8f);
            document.add(noteTitle);

            Paragraph thanks = new Paragraph("Thank you for shopping with Fragrance Haven.", normalFont);
            thanks.setSpacingAfter(6f);
            document.add(thanks);

            Paragraph small = new Paragraph("We appreciate your business.", mutedFont);
            document.add(small);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Cannot generate invoice PDF", e);
        }
    }

    private Paragraph metaParagraph(String label, String value, Font labelFont, Font valueFont) {
        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_RIGHT);
        p.setSpacingAfter(6f);
        p.add(new Phrase(label + ": ", labelFont));
        p.add(new Phrase(value, valueFont));
        return p;
    }

    private void addHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(PRIMARY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(10f);
        cell.setBorderColor(PRIMARY);
        table.addCell(cell);
    }

    private PdfPCell bodyCell(String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(10f);
        cell.setBorderColor(LIGHT_LINE);
        cell.setBackgroundColor(Color.WHITE);
        return cell;
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
        left.setBorderColor(PRIMARY);
        if (highlight) {
            left.setBackgroundColor(SOFT_BG);
        } else {
            left.setBackgroundColor(Color.WHITE);
        }

        PdfPCell right = new PdfPCell(new Phrase(value, rightFont));
        right.setPadding(10f);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setBorder(Rectangle.BOTTOM);
        right.setBorderColor(PRIMARY);
        if (highlight) {
            right.setBackgroundColor(SOFT_BG);
        } else {
            right.setBackgroundColor(Color.WHITE);
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
            if (url == null) return null;
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

    private String safeEnum(Enum<?> e) {
        return e == null ? "-" : e.name();
    }

    private String money(BigDecimal v) {
        BigDecimal value = nvl(v);
        return "$" + value.setScale(2, RoundingMode.HALF_UP);
    }
}