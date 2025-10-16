package com.shop.app.controller;

import com.shop.app.model.Order;
import com.shop.app.model.User;
import com.shop.app.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class OrderController {

    private static final String PAYMENT_METHOD_CASH = "Cash on Delivery";

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders/my")
    public String myOrders(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/order/{orderNumber}")
    public String viewOrder(@PathVariable String orderNumber, Model model) {
        Optional<Order> orderOptional = orderService.getOrderById(orderNumber);
        if (orderOptional.isEmpty()) {
            return "redirect:/products";
        }

        Order order = orderOptional.get();
        model.addAttribute("order", order);
        boolean cardRequired = requiresCard(order.getPaymentMethod());
        model.addAttribute("cardRequired", cardRequired);
        model.addAttribute("cashPaymentMethod", PAYMENT_METHOD_CASH);
        if (cardRequired) {
            model.addAttribute("maskedCardNumber", maskCardNumber(order.getCardLastFour()));
        }

        return "order-confirmed";
    }

    private boolean requiresCard(String paymentMethod) {
        return paymentMethod != null && !PAYMENT_METHOD_CASH.equalsIgnoreCase(paymentMethod.trim());
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank()) {
            return "N/A";
        }
        String value = cardNumber.replaceAll("\\s+", "");
        if (value.length() < 4) {
            return "****";
        }
        String lastFour = value.substring(value.length() - 4);
        return "**** **** **** " + lastFour;
    }
}
