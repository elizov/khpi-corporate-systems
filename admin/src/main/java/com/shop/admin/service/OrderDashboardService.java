package com.shop.admin.service;

import com.shop.admin.dto.DashboardSnapshot;
import com.shop.admin.dto.OrderEventDto;
import com.shop.admin.dto.OrderItemView;
import com.shop.admin.dto.OrderView;
import com.shop.admin.messaging.OrderCancellationMessage;
import com.shop.admin.messaging.OrderConfirmationMessage;
import com.shop.admin.messaging.OrderCreatedMessage;
import com.shop.admin.order.OrderEntity;
import com.shop.admin.order.OrderRepository;
import com.shop.admin.order.OrderStatus;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class OrderDashboardService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withLocale(Locale.getDefault());

    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange orderEventsExchange;
    private final String confirmedRoutingKey;
    private final String canceledRoutingKey;

    public OrderDashboardService(OrderRepository orderRepository,
                                 SimpMessagingTemplate messagingTemplate,
                                 RabbitTemplate rabbitTemplate,
                                 TopicExchange orderEventsExchange,
                                 @Value("${app.messaging.routing.confirmed}") String confirmedRoutingKey,
                                 @Value("${app.messaging.routing.canceled}") String canceledRoutingKey) {
        this.orderRepository = orderRepository;
        this.messagingTemplate = messagingTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.orderEventsExchange = orderEventsExchange;
        this.confirmedRoutingKey = confirmedRoutingKey;
        this.canceledRoutingKey = canceledRoutingKey;
    }

    @Transactional(readOnly = true)
    public DashboardSnapshot loadSnapshot() {
        List<OrderView> newOrders = mapOrders(orderRepository.findAllByStatusOrderByCreatedAtAsc(OrderStatus.NEW));
        List<OrderView> confirmed = mapOrders(orderRepository.findAllByStatusOrderByCreatedAtAsc(OrderStatus.CONFIRMED));
        List<OrderView> canceled = mapOrders(orderRepository.findAllByStatusOrderByCreatedAtAsc(OrderStatus.CANCELED));
        return new DashboardSnapshot(newOrders, confirmed, canceled);
    }

    @Transactional
    public OrderView confirmOrder(String orderId, String comment) {
        OrderEntity order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order %s not found".formatted(orderId)));
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCancellationReason(null);
        if (comment != null && !comment.isBlank()) {
            order.setNotes(comment.trim());
        }
        OrderView view = mapToView(order, null);
        publishConfirmed(order);
        broadcast("CONFIRMED", view);
        return view;
    }

    @Transactional
    public OrderView cancelOrder(String orderId, String reason) {
        String normalizedReason = reason == null ? "" : reason.trim();
        OrderEntity order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order %s not found".formatted(orderId)));
        order.setStatus(OrderStatus.CANCELED);
        order.setCancellationReason(normalizedReason);
        OrderView view = mapToView(order, null);
        publishCanceled(order, normalizedReason);
        broadcast("CANCELED", view);
        return view;
    }

    @Transactional(readOnly = true)
    public void handleIncomingOrder(OrderCreatedMessage message) {
        Optional<OrderEntity> orderOptional = orderRepository.findWithItemsById(message.orderId());
        if (orderOptional.isEmpty()) {
            return;
        }
        OrderView view = mapToView(orderOptional.get(), message.username());
        broadcast("NEW", view);
    }

    private void publishConfirmed(OrderEntity order) {
        List<OrderConfirmationMessage.OrderLine> lines = order.getItems().stream()
                .map(item -> new OrderConfirmationMessage.OrderLine(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity()
                ))
                .toList();
        OrderConfirmationMessage message = new OrderConfirmationMessage(order.getId(), lines);
        rabbitTemplate.convertAndSend(orderEventsExchange.getName(), confirmedRoutingKey, message);
    }

    private void publishCanceled(OrderEntity order, String reason) {
        BigDecimal lostAmount = Optional.ofNullable(order.getTotalPrice()).orElse(BigDecimal.ZERO);
        OrderCancellationMessage message = new OrderCancellationMessage(order.getId(), reason, lostAmount);
        rabbitTemplate.convertAndSend(orderEventsExchange.getName(), canceledRoutingKey, message);
    }

    private void broadcast(String type, OrderView order) {
        messagingTemplate.convertAndSend("/topic/orders", new OrderEventDto(type, order));
    }

    private List<OrderView> mapOrders(List<OrderEntity> orders) {
        return orders.stream()
                .map(order -> mapToView(order, null))
                .toList();
    }

    private OrderView mapToView(OrderEntity order, String preferredCustomerName) {
        String customerName = preferredCustomerName != null && !preferredCustomerName.isBlank()
                ? preferredCustomerName
                : order.getFullName();

        List<OrderItemView> items = order.getItems().stream()
                .map(item -> new OrderItemView(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderView(
                order.getId(),
                customerName,
                order.getEmail(),
                order.getPhone(),
                order.getAddress(),
                order.getCity(),
                order.getDeliveryMethod(),
                order.getPaymentMethod(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getTotalQuantity(),
                order.getCreatedAt().format(DATE_FORMATTER),
                order.getNotes(),
                order.getCancellationReason(),
                items
        );
    }
}
