package com.shop.admin.messaging;

import java.util.List;

public record OrderConfirmationMessage(
        String orderId,
        List<OrderLine> items
) {
    public record OrderLine(
            Long productId,
            String productName,
            Integer quantity
    ) {
    }
}
