package com.shop.app.controller;

import com.shop.app.dto.OrderResponse;
import com.shop.app.model.Order;
import com.shop.app.model.User;
import com.shop.app.service.OrderService;
import jakarta.servlet.http.HttpSession;
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
    public ResponseEntity<?> myOrders(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        return ResponseEntity.ok(orders.stream().map(OrderResponse::from).toList());
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<?> viewOrder(@PathVariable String orderNumber, HttpSession session) {
        Optional<Order> orderOptional = orderService.getOrderById(orderNumber);
        if (orderOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Order order = orderOptional.get();
        User sessionUser = (User) session.getAttribute("user");
        if (order.getUser() != null && (sessionUser == null || !order.getUser().getId().equals(sessionUser.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return ResponseEntity.ok(OrderResponse.from(order));
    }
}
