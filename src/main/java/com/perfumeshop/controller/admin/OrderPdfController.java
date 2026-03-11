package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Order;
import com.perfumeshop.service.InvoicePdfService;
import com.perfumeshop.service.OrderService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
public class OrderPdfController {

    private final OrderService orderService;
    private final InvoicePdfService invoicePdfService;

    public OrderPdfController(OrderService orderService,
                                   InvoicePdfService invoicePdfService) {
        this.orderService = orderService;
        this.invoicePdfService = invoicePdfService;
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
        Order order = orderService.findWithItemsOrThrow(id);
        byte[] pdf = invoicePdfService.generate(order);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=invoice-" + order.getInvoice() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}