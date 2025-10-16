package com.shop.app.service;

import com.shop.app.model.CartItem;
import com.shop.app.model.CheckoutForm;
import com.shop.app.model.Order;
import com.shop.app.model.OrderItem;
import com.shop.app.model.User;
import com.shop.app.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrder(CheckoutForm checkoutForm,
                             List<CartItem> items,
                             int totalQuantity,
                             BigDecimal totalPrice,
                             User user) {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setCreatedAt(LocalDateTime.now());
        order.setFullName(checkoutForm.getFullName());
        order.setEmail(checkoutForm.getEmail());
        order.setPhone(checkoutForm.getPhone());
        order.setAddress(checkoutForm.getAddress());
        order.setCity(checkoutForm.getCity());
        order.setPostalCode(checkoutForm.getPostalCode());
        order.setDeliveryMethod(checkoutForm.getDeliveryMethod());
        order.setPaymentMethod(checkoutForm.getPaymentMethod());
        order.setNotes(checkoutForm.getNotes());
        order.setTotalQuantity(totalQuantity);
        order.setTotalPrice(totalPrice == null ? BigDecimal.ZERO : totalPrice.setScale(2, RoundingMode.HALF_UP));
        order.setUser(user);

        String cardNumber = checkoutForm.getCardNumber();
        if (cardNumber != null && cardNumber.length() >= 4) {
            String digits = cardNumber.replaceAll("\\s+", "");
            if (digits.length() >= 4) {
                order.setCardLastFour(digits.substring(digits.length() - 4));
            }
        }

        for (CartItem cartItem : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            BigDecimal unitPrice = cartItem.getPrice() == null
                    ? BigDecimal.ZERO
                    : cartItem.getPrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = cartItem.getSubtotal() == null
                    ? unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                    : cartItem.getSubtotal().setScale(2, RoundingMode.HALF_UP);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);
            order.getItems().add(orderItem);
        }

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return orderRepository.findWithItemsById(id);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
}
