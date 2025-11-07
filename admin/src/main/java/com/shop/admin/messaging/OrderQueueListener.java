package com.shop.admin.messaging;

import com.shop.admin.service.OrderDashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderQueueListener {

    private static final Logger log = LoggerFactory.getLogger(OrderQueueListener.class);

    private final OrderDashboardService dashboardService;

    public OrderQueueListener(OrderDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @RabbitListener(queues = "${app.messaging.order-queue}")
    public void onOrderCreated(OrderCreatedMessage message) {
        log.info("Received order {} for user {}", message.orderId(), message.username());
        dashboardService.handleIncomingOrder(message);
    }
}
