package com.shop.admin.controller;

import com.shop.admin.dto.OrderApprovalRequest;
import com.shop.admin.dto.OrderCancellationRequest;
import com.shop.admin.dto.OrderEventDto;
import com.shop.admin.service.OrderDashboardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderActionController {

    private final OrderDashboardService dashboardService;

    public OrderActionController(OrderDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderEventDto> confirm(@PathVariable String orderId,
                                                 @RequestBody(required = false) OrderApprovalRequest request) {
        var view = dashboardService.confirmOrder(orderId, request != null ? request.comment() : null);
        return ResponseEntity.ok(new OrderEventDto("CONFIRMED", view));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderEventDto> cancel(@PathVariable String orderId,
                                                @Valid @RequestBody OrderCancellationRequest request) {
        var view = dashboardService.cancelOrder(orderId, request.reason());
        return ResponseEntity.ok(new OrderEventDto("CANCELED", view));
    }
}
