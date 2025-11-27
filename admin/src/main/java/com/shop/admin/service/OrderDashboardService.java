package com.shop.admin.service;

import com.shop.admin.client.OrderClient;
import com.shop.admin.dto.OrderView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDashboardService {

    private final OrderClient orderClient;

    public OrderDashboardService(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    public List<OrderView> listAll(HttpServletRequest request) {
        return orderClient.listAll(request);
    }

    public OrderView confirmOrder(String orderId, String comment, HttpServletRequest request) {
        return orderClient.confirm(orderId, comment, request);
    }

    public OrderView cancelOrder(String orderId, String reason, HttpServletRequest request) {
        return orderClient.cancel(orderId, reason, request);
    }
}
