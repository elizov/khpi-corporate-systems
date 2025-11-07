package com.shop.admin.messaging;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedMessage(
        String orderId,
        String username,
        List<OrderCreatedItem> items,
        BigDecimal totalPrice
) {

    public record OrderCreatedItem(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal subtotal
    ) {
    }
}
