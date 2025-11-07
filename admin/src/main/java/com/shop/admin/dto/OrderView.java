package com.shop.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderView(
        String id,
        String customerName,
        String email,
        String phone,
        String address,
        String city,
        String deliveryMethod,
        String paymentMethod,
        String status,
        BigDecimal totalPrice,
        Integer totalQuantity,
        String createdAt,
        String notes,
        String cancellationReason,
        List<OrderItemView> items
) {
}
