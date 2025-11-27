package com.shop.admin.controller;

import com.shop.admin.dto.OrderApprovalRequest;
import com.shop.admin.dto.OrderCancellationRequest;
import com.shop.admin.service.OrderDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
public class OrderActionController {

    private final OrderDashboardService dashboardService;

    public OrderActionController(OrderDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<?> all(HttpServletRequest request) {
        return ResponseEntity.ok(dashboardService.listAll(request));
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<?> confirm(@PathVariable String orderId,
                                                 @RequestBody(required = false) OrderApprovalRequest request,
                                                 HttpServletRequest servletRequest) {
        return ResponseEntity.ok(dashboardService.confirmOrder(
                orderId,
                request != null ? request.comment() : null,
                servletRequest
        ));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String orderId,
                                                @Valid @RequestBody OrderCancellationRequest request,
                                                HttpServletRequest servletRequest) {
        return ResponseEntity.ok(dashboardService.cancelOrder(
                orderId,
                request.reason(),
                servletRequest
        ));
    }
}
