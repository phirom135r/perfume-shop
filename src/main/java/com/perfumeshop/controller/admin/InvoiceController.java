package com.perfumeshop.controller.admin;

import com.perfumeshop.entity.Order;
import com.perfumeshop.service.InvoicePdfService;
import com.perfumeshop.service.OrderService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
public class InvoiceController {

    private final OrderService orderService;
    private final InvoicePdfService invoicePdfService;

    public InvoiceController(OrderService orderService,
                             InvoicePdfService invoicePdfService) {
        this.orderService = orderService;
        this.invoicePdfService = invoicePdfService;
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
        Order order = orderService.findInvoiceForAdminOrThrow(id);
        byte[] pdf = invoicePdfService.generate(order);

        String filename = (order.getInvoice() != null && !order.getInvoice().isBlank())
                ? order.getInvoice() + ".pdf"
                : "invoice-" + order.getId() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(filename).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}