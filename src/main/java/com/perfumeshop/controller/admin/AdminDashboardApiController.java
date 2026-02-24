//AdminDashboardApiController
package com.perfumeshop.controller.admin;

import com.perfumeshop.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/dashboard")
public class AdminDashboardApiController {

    private final DashboardService dashboardService;

    public AdminDashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // range: TODAY | LAST_7_DAYS | THIS_MONTH | CUSTOM
    // custom: from=yyyy-MM-dd  to=yyyy-MM-dd
    @GetMapping("/summary")
    public ResponseEntity<?> summary(
            @RequestParam(defaultValue = "LAST_7_DAYS") String range,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return ResponseEntity.ok(dashboardService.summary(range, from, to));
    }

    @GetMapping("/sales")
    public ResponseEntity<?> sales(
            @RequestParam(defaultValue = "LAST_7_DAYS") String range,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return ResponseEntity.ok(dashboardService.sales(range, from, to));
    }

    @GetMapping("/top-products")
    public ResponseEntity<?> topProducts(
            @RequestParam(defaultValue = "LAST_7_DAYS") String range,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return ResponseEntity.ok(dashboardService.topProducts(range, from, to));
    }
}