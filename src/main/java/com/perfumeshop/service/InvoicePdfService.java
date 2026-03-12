package com.perfumeshop.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
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
import java.time.format.DateTimeFormatter;

@Service
public class InvoicePdfService {

    // Modern color palette
    private static final Color PRIMARY_GOLD = new Color(191, 155, 96);
    private static final Color DARK_TEXT = new Color(35, 35, 35);
    private static final Color MUTED_TEXT = new Color(120, 120, 120);
    private static final Color LIGHT_BG = new Color(250, 248, 245);
    private static final Color BORDER_COLOR = new Color(230, 225, 215);
    private static final Color ACCENT_RED = new Color(190, 50, 50);
    private static final Color TABLE_HEADER = new Color(191, 155, 96);
    private static final Color TABLE_ROW_ALT = new Color(252, 251, 249);

    private static final String KHMER_FONT_PATH = "/fonts/Hanuman-VariableFont_wght.ttf";

    public byte[] generate(Order order) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // ===== FONTS =====
            Font brandFont = new Font(Font.TIMES_ROMAN, 32, Font.BOLD, PRIMARY_GOLD);
            Font invoiceTitleFont = new Font(Font.HELVETICA, 26, Font.BOLD, PRIMARY_GOLD);
            Font sectionTitleFont = new Font(Font.HELVETICA, 12, Font.BOLD, PRIMARY_GOLD);
            Font labelFont = new Font(Font.HELVETICA, 9, Font.BOLD, MUTED_TEXT);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, DARK_TEXT);
            Font mutedFont = new Font(Font.HELVETICA, 9, Font.NORMAL, MUTED_TEXT);
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, DARK_TEXT);
            Font tableHeaderFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            Font totalLabelFont = new Font(Font.HELVETICA, 11, Font.BOLD, DARK_TEXT);
            Font totalValueFont = new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY_GOLD);
            Font redFont = new Font(Font.HELVETICA, 10, Font.BOLD, ACCENT_RED);

            // Khmer font
            BaseFont khmerBase = BaseFont.createFont(
                    KHMER_FONT_PATH,
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );
            Font khmerNormal = new Font(khmerBase, 10, Font.NORMAL, DARK_TEXT);
            Font khmerBold = new Font(khmerBase, 11, Font.BOLD, DARK_TEXT);

            // ===== HEADER SECTION =====
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60f, 40f});
            headerTable.setSpacingAfter(30f);

            // Left: Brand info
            PdfPCell brandCell = new PdfPCell();
            brandCell.setBorder(Rectangle.NO_BORDER);
            brandCell.setVerticalAlignment(Element.ALIGN_TOP);

            Paragraph brandName = new Paragraph("Fragrance Haven", brandFont);
            brandName.setSpacingAfter(8f);
            brandCell.addElement(brandName);

            brandCell.addElement(new Paragraph("Premium Perfume Collection", mutedFont));
            Paragraph address = new Paragraph("Street 271, Phnom Penh, Cambodia", mutedFont);
            address.setSpacingAfter(2f);
            brandCell.addElement(address);
            brandCell.addElement(new Paragraph("Tel: +855 12 345 678", mutedFont));
            brandCell.addElement(new Paragraph("Email: support@fragrancehaven.com", mutedFont));

            // Right: Invoice info
            PdfPCell invoiceInfoCell = new PdfPCell();
            invoiceInfoCell.setBorder(Rectangle.NO_BORDER);
            invoiceInfoCell.setVerticalAlignment(Element.ALIGN_TOP);

            Paragraph invoiceTitle = new Paragraph("INVOICE", invoiceTitleFont);
            invoiceTitle.setAlignment(Element.ALIGN_RIGHT);
            invoiceTitle.setSpacingAfter(16f);
            invoiceInfoCell.addElement(invoiceTitle);

            invoiceInfoCell.addElement(createInfoRow("Invoice #", safe(order.getInvoice()), labelFont, boldFont));
            invoiceInfoCell.addElement(createInfoRow("Date", formatDate(order), labelFont, normalFont));
            invoiceInfoCell.addElement(createInfoRow("Payment", prettyEnum(order.getPaymentMethod()), labelFont, normalFont));
            invoiceInfoCell.addElement(createInfoRow("Status", prettyEnum(order.getStatus()), labelFont, normalFont));

            headerTable.addCell(brandCell);
            headerTable.addCell(invoiceInfoCell);
            document.add(headerTable);

            // ===== BILLED TO SECTION =====
            Paragraph billedToTitle = new Paragraph("BILLED TO", sectionTitleFont);
            billedToTitle.setSpacingBefore(10f);
            billedToTitle.setSpacingAfter(10f);
            document.add(billedToTitle);

            PdfPTable billedToBox = new PdfPTable(1);
            billedToBox.setWidthPercentage(100);
            billedToBox.setSpacingAfter(25f);

            PdfPCell billedCell = new PdfPCell();
            billedCell.setPadding(16f);
            billedCell.setBorderColor(BORDER_COLOR);
            billedCell.setBorderWidth(1.5f);
            billedCell.setBackgroundColor(LIGHT_BG);

            Paragraph customerName = new Paragraph(safe(order.getCustomerName()), khmerBold);
            customerName.setSpacingAfter(6f);
            billedCell.addElement(customerName);

            billedCell.addElement(new Paragraph("Phone: " + safe(order.getPhone()), khmerNormal));
            billedCell.addElement(new Paragraph("Address: " + safe(order.getAddress()), khmerNormal));

            billedToBox.addCell(billedCell);
            document.add(billedToBox);

            // ===== ITEMS TABLE =====
            Paragraph itemsTitle = new Paragraph("ORDER DETAILS", sectionTitleFont);
            itemsTitle.setSpacingBefore(10f);
            itemsTitle.setSpacingAfter(10f);
            document.add(itemsTitle);

            PdfPTable itemsTable = new PdfPTable(6);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{32f, 10f, 14f, 14f, 14f, 16f});
            itemsTable.setSpacingAfter(25f);

            // Table headers
            addStyledHeader(itemsTable, "DESCRIPTION", tableHeaderFont);
            addStyledHeader(itemsTable, "QTY", tableHeaderFont);
            addStyledHeader(itemsTable, "ORIGINAL", tableHeaderFont);
            addStyledHeader(itemsTable, "UNIT", tableHeaderFont);
            addStyledHeader(itemsTable, "DISC.", tableHeaderFont);
            addStyledHeader(itemsTable, "AMOUNT", tableHeaderFont);

            // Table rows
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                int rowIndex = 0;
                for (OrderItem item : order.getItems()) {
                    String productName = item.getProduct() != null ? safe(item.getProduct().getName()) : "-";
                    String size = (item.getProduct() != null &&
                            item.getProduct().getSize() != null &&
                            !item.getProduct().getSize().isBlank())
                            ? " / " + item.getProduct().getSize()
                            : "";

                    BigDecimal originalPrice = nvl(item.getOriginalPrice());
                    BigDecimal unitPrice = nvl(item.getUnitPrice());
                    BigDecimal discountAmount = nvl(item.getDiscountAmount());
                    BigDecimal lineTotal = nvl(item.getLineTotal());

                    Color rowBg = (rowIndex % 2 == 0) ? Color.WHITE : TABLE_ROW_ALT;

                    itemsTable.addCell(createStyledCell(productName + size, normalFont, Element.ALIGN_LEFT, rowBg));
                    itemsTable.addCell(createStyledCell(String.valueOf(item.getQty()), normalFont, Element.ALIGN_CENTER, rowBg));
                    itemsTable.addCell(createStyledCell(money(originalPrice), normalFont, Element.ALIGN_RIGHT, rowBg));
                    itemsTable.addCell(createStyledCell(money(unitPrice), boldFont, Element.ALIGN_RIGHT, rowBg));
                    itemsTable.addCell(createStyledCell(money(discountAmount), redFont, Element.ALIGN_RIGHT, rowBg));
                    itemsTable.addCell(createStyledCell(money(lineTotal), boldFont, Element.ALIGN_RIGHT, rowBg));

                    rowIndex++;
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No items in this order", mutedFont));
                emptyCell.setColspan(6);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(20f);
                emptyCell.setBorderColor(BORDER_COLOR);
                emptyCell.setBackgroundColor(LIGHT_BG);
                itemsTable.addCell(emptyCell);
            }

            document.add(itemsTable);

            // ===== SUMMARY SECTION =====
            PdfPTable summaryWrapper = new PdfPTable(2);
            summaryWrapper.setWidthPercentage(100);
            summaryWrapper.setWidths(new float[]{55f, 45f});
            summaryWrapper.setSpacingAfter(35f);

            PdfPCell leftSpace = new PdfPCell(new Phrase(""));
            leftSpace.setBorder(Rectangle.NO_BORDER);

            PdfPCell summaryCell = new PdfPCell();
            summaryCell.setBorder(Rectangle.NO_BORDER);
            summaryCell.setPadding(0f);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setWidths(new float[]{50f, 50f});

            addSummaryRow(summaryTable, "Subtotal", money(order.getSubtotal()), normalFont, boldFont, false);

            if (nvl(order.getDiscount()).compareTo(BigDecimal.ZERO) > 0) {
                addSummaryRow(summaryTable, "Discount", "- " + money(order.getDiscount()), normalFont, redFont, false);
            }

            addSummaryRow(summaryTable, "GRAND TOTAL", money(order.getTotal()), totalLabelFont, totalValueFont, true);

            summaryCell.addElement(summaryTable);

            summaryWrapper.addCell(leftSpace);
            summaryWrapper.addCell(summaryCell);

            document.add(summaryWrapper);

            // ===== FOOTER =====
            Paragraph footerTitle = new Paragraph("THANK YOU FOR YOUR BUSINESS", sectionTitleFont);
            footerTitle.setAlignment(Element.ALIGN_CENTER);
            footerTitle.setSpacingAfter(8f);
            document.add(footerTitle);

            Paragraph footerText = new Paragraph("We appreciate your trust in Fragrance Haven.", normalFont);
            footerText.setAlignment(Element.ALIGN_CENTER);
            footerText.setSpacingAfter(4f);
            document.add(footerText);

            Paragraph footerKhmer = new Paragraph("សូមអរគុណសម្រាប់ការទិញទំនិញ!", khmerNormal);
            footerKhmer.setAlignment(Element.ALIGN_CENTER);
            footerKhmer.setSpacingAfter(10f);
            document.add(footerKhmer);

            Paragraph footerNote = new Paragraph("Questions? Contact us at support@fragrancehaven.com", mutedFont);
            footerNote.setAlignment(Element.ALIGN_CENTER);
            document.add(footerNote);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Cannot generate invoice PDF: " + e.getMessage(), e);
        }
    }

    // ===== HELPER METHODS =====

    private Paragraph createInfoRow(String label, String value, Font labelFont, Font valueFont) {
        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_RIGHT);
        p.setSpacingAfter(6f);
        p.add(new Phrase(label + ": ", labelFont));
        p.add(new Phrase(value, valueFont));
        return p;
    }

    private void addStyledHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(TABLE_HEADER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(10f);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private PdfPCell createStyledCell(String text, Font font, int alignment, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(10f);
        cell.setBorderColor(BORDER_COLOR);
        cell.setBorderWidth(0.5f);
        cell.setBackgroundColor(bgColor);
        return cell;
    }

    private void addSummaryRow(PdfPTable table, String label, String value,
                               Font labelFont, Font valueFont, boolean isTotal) {

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(12f);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setBorder(isTotal ? Rectangle.TOP : Rectangle.NO_BORDER);
        labelCell.setBorderColor(BORDER_COLOR);
        labelCell.setBorderWidth(isTotal ? 2f : 0f);
        if (isTotal) {
            labelCell.setBackgroundColor(LIGHT_BG);
        }

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(12f);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBorder(isTotal ? Rectangle.TOP : Rectangle.NO_BORDER);
        valueCell.setBorderColor(BORDER_COLOR);
        valueCell.setBorderWidth(isTotal ? 2f : 0f);
        if (isTotal) {
            valueCell.setBackgroundColor(LIGHT_BG);
        }

        table.addCell(labelCell);
        table.addCell(valueCell);
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
        return "$" + value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
