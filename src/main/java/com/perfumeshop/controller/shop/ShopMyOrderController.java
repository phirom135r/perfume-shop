package com.perfumeshop.controller.shop;

import com.perfumeshop.config.SecurityHelper;
import com.perfumeshop.entity.Order;
import com.perfumeshop.service.InvoicePdfService;
import com.perfumeshop.service.OrderService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perfume-shop/my-orders")
public class ShopMyOrderController {

    private final OrderService orderService;
    private final InvoicePdfService invoicePdfService;

    public ShopMyOrderController(OrderService orderService,
                                 InvoicePdfService invoicePdfService) {
        this.orderService = orderService;
        this.invoicePdfService = invoicePdfService;
    }

    @GetMapping
    public String myOrders(Authentication authentication, Model model) {
        String email = SecurityHelper.currentCustomerEmail(authentication);
        model.addAttribute("orders", orderService.findMyOrders(email));
        return "shop/my-orders";
    }

    @GetMapping("/{id}")
    public String myOrderDetail(@PathVariable Long id,
                                Authentication authentication,
                                Model model) {
        String email = SecurityHelper.currentCustomerEmail(authentication);
        model.addAttribute("order", orderService.findMyOrderDetailOrThrow(id, email));
        return "shop/my-order-detail";
    }

    @GetMapping("/{id}/invoice")
    @ResponseBody
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id,
                                                  Authentication authentication) {
        String email = SecurityHelper.currentCustomerEmail(authentication);
        Order order = orderService.findInvoiceForCustomerOrThrow(id, email);

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