package com.shop.admin.dto;

import java.util.List;

public record DashboardSnapshot(
        List<OrderView> newOrders,
        List<OrderView> confirmedOrders,
        List<OrderView> canceledOrders
) {
}
