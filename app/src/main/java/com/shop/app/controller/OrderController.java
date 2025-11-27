package com.shop.app.controller;

import com.shop.app.dto.OrderResponse;
import com.shop.app.model.Order;
import com.shop.app.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/my")
    public ResponseEntity<?> myOrders(HttpSession session, HttpServletRequest request) {
        Long userId = resolveUserId(session, request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders.stream().map(OrderResponse::from).toList());
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<?> viewOrder(@PathVariable String orderNumber,
                                       HttpSession session,
                                       HttpServletRequest request) {
        Optional<Order> orderOptional = orderService.getOrderById(orderNumber);
        if (orderOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Order order = orderOptional.get();
        Long userId = resolveUserId(session, request);
        if (order.getUserId() != null && (userId == null || !order.getUserId().equals(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return ResponseEntity.ok(OrderResponse.from(order));
    }

    private Long resolveUserId(HttpSession session, HttpServletRequest request) {
        Object cached = session.getAttribute("userId");
        if (cached instanceof Long) {
            return (Long) cached;
        }

        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return null;
        }
        try {
            Long parsed = Long.parseLong(userIdHeader);
            session.setAttribute("userId", parsed);
            return parsed;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
