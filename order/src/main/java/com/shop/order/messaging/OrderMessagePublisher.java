package com.shop.order.messaging;

import com.shop.order.model.Order;
import com.shop.order.model.OrderItem;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class OrderMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String orderQueue;

    public OrderMessagePublisher(RabbitTemplate rabbitTemplate,
                                 @Value("${app.messaging.queues.new}") String orderQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderQueue = orderQueue;
    }

    public void publishOrderCreated(Order order) {
        if (order == null) {
            return;
        }

        OrderCreatedMessage payload = toPayload(order);
        Runnable sendTask = () -> rabbitTemplate.convertAndSend(orderQueue, payload);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendTask.run();
                }
            });
        } else {
            sendTask.run();
        }
    }

    private OrderCreatedMessage toPayload(Order order) {
        String username = Optional.ofNullable(order.getUserId())
                .map(id -> "user-" + id)
                .orElse(order.getFullName());

        List<OrderItem> sourceItems = order.getItems() == null ? List.of() : order.getItems();
        List<OrderItemMessage> items = sourceItems.stream()
                .map(item -> new OrderItemMessage(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderCreatedMessage(
                order.getId(),
                username,
                items,
                order.getTotalPrice()
        );
    }

    public record OrderCreatedMessage(
            String orderId,
            String username,
            List<OrderItemMessage> items,
            BigDecimal totalPrice
    ) {}

    public record OrderItemMessage(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal subtotal
    ) {}
}
