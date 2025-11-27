package com.shop.order.controller;

import com.shop.order.dto.OrderResponse;
import com.shop.order.model.Order;
import com.shop.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> all() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders.stream().map(OrderResponse::from).toList());
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponse> confirm(@PathVariable String orderId,
                                                 @RequestBody(required = false) ConfirmRequest request) {
        Order order = orderService.confirmOrder(orderId, request != null ? request.comment : null);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable String orderId,
                                                @Valid @RequestBody CancelRequest request) {
        Order order = orderService.cancelOrder(orderId, request.reason);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    public record ConfirmRequest(String comment) {}

    public static class CancelRequest {
        @NotBlank
        public String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
